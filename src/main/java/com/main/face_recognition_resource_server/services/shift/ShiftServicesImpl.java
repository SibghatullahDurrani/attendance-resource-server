package com.main.face_recognition_resource_server.services.shift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.*;
import com.main.face_recognition_resource_server.constants.RabbitMQMessageType;
import com.main.face_recognition_resource_server.constants.ShiftMessageType;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.Shift;
import com.main.face_recognition_resource_server.repositories.shift.ShiftRepository;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import com.main.face_recognition_resource_server.utilities.MessageMetadataWrapper;
import com.rabbitmq.client.Channel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftServicesImpl implements ShiftServices {
    private final ShiftRepository shiftRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String CONTROL_EXCHANGE_NAME = "control.exchange";
    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(ShiftServicesImpl.class);
    private final RabbitMQMessageBackupServices rabbitMQMessageBackupServices;

    public ShiftServicesImpl(ShiftRepository shiftRepository, RabbitTemplate rabbitTemplate, ObjectMapper mapper, RabbitMQMessageBackupServices rabbitMQMessageBackupServices) {
        this.shiftRepository = shiftRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.rabbitMQMessageBackupServices = rabbitMQMessageBackupServices;
    }

    @Override
    public void registerShift(RegisterShiftDTO registerShiftDTO, Long organizationId) throws SQLException {
        Shift shiftToRegister = Shift.builder()
                .name(registerShiftDTO.getName())
                .checkInTime(registerShiftDTO.getCheckInTime())
                .checkOutTime(registerShiftDTO.getCheckOutTime())
                .organization(Organization.builder().id(organizationId).build())
                .build();
        Long organizationShiftsCount = shiftRepository.getOrganizationShiftCount(organizationId);
        shiftToRegister.setDefault(organizationShiftsCount <= 0);
        Shift registeredShift = shiftRepository.saveAndFlush(shiftToRegister);
        try {
            String SHIFT_CONTROL_ROUTING_KEY = "control." + organizationId + ".shift.key";

            ShiftCreationMessageDTO shiftCreationMessageDTO = ShiftCreationMessageDTO.builder()
                    .id(registeredShift.getId())
                    .name(registeredShift.getName())
                    .checkInTime(registerShiftDTO.getCheckInTime())
                    .checkOutTime(registerShiftDTO.getCheckOutTime())
                    .build();

            ShiftMessageDTO shiftMessageDTO = ShiftMessageDTO.builder()
                    .shiftMessageType(ShiftMessageType.CREATE_SHIFT)
                    .payload(shiftCreationMessageDTO)
                    .build();

            UUID backupMessageId = rabbitMQMessageBackupServices.backupMessageAndReturnId(shiftMessageDTO, organizationId);
            String shiftMessageJson = mapper.writeValueAsString(shiftMessageDTO);

            String messageMetadataWrapper = mapper.writeValueAsString(
                    new MessageMetadataWrapper(backupMessageId, RabbitMQMessageType.SHIFT)
            );
            CorrelationData correlationData = new CorrelationData(messageMetadataWrapper);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uuid", backupMessageId.toString());
            messageProperties.setHeader("routingType", RabbitMQMessageType.SHIFT.name());
            Message message = new Message(shiftMessageJson.getBytes(StandardCharsets.UTF_8));

            rabbitTemplate.convertAndSend(CONTROL_EXCHANGE_NAME, SHIFT_CONTROL_ROUTING_KEY, message, correlationData);
        } catch (JsonProcessingException e) {
            logger.error("Error Converting Message To JSON");
        } catch (AmqpException e) {
            logger.error("Error sending message");
        }
    }

    @Override
    public Page<ShiftTableRowDTO> getShiftsPage(Long organizationId, String name, String checkInTime, String checkOutTime, PageRequest pageRequest) {
        Specification<Shift> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Shift, Organization> shiftOrganizationJoin = root.join("organization", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(shiftOrganizationJoin.get("id"), organizationId));
            if (name != null) {
                String nameLower = name.toLowerCase();
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("name")), nameLower));
            }
            if (checkInTime != null) {
                predicates.add(criteriaBuilder.equal(root.get("checkInTime"), checkInTime));
            }
            if (checkOutTime != null) {
                predicates.add(criteriaBuilder.equal(root.get("checkOutTime"), checkOutTime));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return shiftRepository.getShifts(specification, pageRequest);
    }

    @Override
    @RabbitListener(queues = {"${shift-control-acknowledgement-queue-name}"})
    public void handleShiftAcknowledgementMessage(Message message, Channel channel) throws IOException {
        try {
            byte[] messageBody = message.getBody();
            ShiftControlAcknowledgementDTO shiftControlAcknowledgementDTO = mapper.readValue(messageBody, ShiftControlAcknowledgementDTO.class);
            shiftRepository.findById(shiftControlAcknowledgementDTO.getId()).ifPresent(shiftControlAcknowledgement -> {
                shiftControlAcknowledgement.setSavedInProducer(true);
                shiftControlAcknowledgement.setLastSavedInProducerDate(new Date());
                shiftRepository.saveAndFlush(shiftControlAcknowledgement);
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            logger.error("Error Converting Message To JSON");
        }

    }

    @Override
    public List<ShiftOptionDTO> getShiftOptions(Long organizationId) {
        return shiftRepository.getShiftOptionsByOrganizationId(organizationId);
    }
}
