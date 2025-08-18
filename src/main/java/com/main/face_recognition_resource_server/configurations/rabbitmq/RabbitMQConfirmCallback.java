package com.main.face_recognition_resource_server.configurations.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupService;
import com.main.face_recognition_resource_server.utilities.MessageMetadataWrapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RabbitMQConfirmCallback implements RabbitTemplate.ConfirmCallback {
    private final RabbitMQMessageBackupService rabbitMQMessageBackupService;
    private final ObjectMapper mapper;

    public RabbitMQConfirmCallback(RabbitMQMessageBackupService rabbitMQMessageBackupService, ObjectMapper mapper) {
        this.rabbitMQMessageBackupService = rabbitMQMessageBackupService;
        this.mapper = mapper;
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (correlationData == null) {
            return;
        }
        try {
            MessageMetadataWrapper metaDataWrapper = mapper.readValue(correlationData.getId(), MessageMetadataWrapper.class);
            UUID backupMessageId = mapper.convertValue(metaDataWrapper.backupMessageId(), UUID.class);
            RabbitMQMessageBackup messageBackup = rabbitMQMessageBackupService.getBackupMessage(backupMessageId);

            if (!ack) {
                messageBackup.setMessageStatus(MessageStatus.PENDING);
            } else {
                messageBackup.setMessageStatus(MessageStatus.DELIVERED);
            }
            rabbitMQMessageBackupService.backupMessage(messageBackup);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
