package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessageDTO;
import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.constants.RabbitMQMessageType;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.repositories.RabbitMQMessageBackupRepository;
import com.main.face_recognition_resource_server.utilities.MessageMetadataWrapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitMQMessageBackupServicesImpl implements RabbitMQMessageBackupServices {
    private final RabbitMQMessageBackupRepository rabbitMQMessageBackupRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQMessageBackupServicesImpl(RabbitMQMessageBackupRepository rabbitMQMessageBackupRepository, ObjectMapper objectMapper, @Lazy RabbitTemplate rabbitTemplate) {
        this.rabbitMQMessageBackupRepository = rabbitMQMessageBackupRepository;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public UUID backupMessageAndReturnId(ShiftMessageDTO shiftMessageDTO, Long organizationId) throws JsonProcessingException {
        String shiftMessageString = objectMapper.writeValueAsString(shiftMessageDTO);
        RabbitMQMessageBackup backup = RabbitMQMessageBackup.builder()
                .message(shiftMessageString)
                .messageStatus(MessageStatus.SENT)
                .organizationId(organizationId)
                .build();

        RabbitMQMessageBackup message = rabbitMQMessageBackupRepository.saveAndFlush(backup);
        return message.getId();
    }

    @Override
    public void backupMessage(RabbitMQMessageBackup rabbitMQMessageBackup) {
        rabbitMQMessageBackupRepository.saveAndFlush(rabbitMQMessageBackup);
    }

    @Override
    public RabbitMQMessageBackup getBackupMessage(UUID backupMessageId) {
        return rabbitMQMessageBackupRepository.findById(backupMessageId).orElseThrow();
    }

    @Override
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void resendPendingMessages() throws JsonProcessingException {
        List<RabbitMQMessageBackup> pendingMessages = rabbitMQMessageBackupRepository.getPendingMessages();
        for (RabbitMQMessageBackup pendingMessage : pendingMessages) {
            String messageMetadataWrapper = objectMapper.writeValueAsString(
                    new MessageMetadataWrapper(pendingMessage.getId(), RabbitMQMessageType.SHIFT)
            );
            CorrelationData correlationData = new CorrelationData(messageMetadataWrapper);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uuid", pendingMessage.getId().toString());
            messageProperties.setHeader("routingType", RabbitMQMessageType.SHIFT.name());
            Message message = new Message(pendingMessage.getMessage().getBytes(StandardCharsets.UTF_8));

            String CONTROL_EXCHANGE_NAME = "control.exchange";
            String SHIFT_CONTROL_ROUTING_KEY = "control." + pendingMessage.getOrganizationId() + ".shift.key";
            rabbitTemplate.convertAndSend(CONTROL_EXCHANGE_NAME, SHIFT_CONTROL_ROUTING_KEY, message, correlationData);
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteDeliveredMessages() {
        List<UUID> deliveredMessagesIds = rabbitMQMessageBackupRepository.getAllDeliveredMessagesIds();
        rabbitMQMessageBackupRepository.deleteAllById(deliveredMessagesIds);
    }
}
