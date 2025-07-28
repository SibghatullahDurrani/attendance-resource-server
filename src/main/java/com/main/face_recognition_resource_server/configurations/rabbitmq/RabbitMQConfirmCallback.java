package com.main.face_recognition_resource_server.configurations.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import com.main.face_recognition_resource_server.utilities.MessageMetadataWrapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RabbitMQConfirmCallback implements RabbitTemplate.ConfirmCallback {
    private final RabbitMQMessageBackupServices rabbitMQMessageBackupServices;
    private final ObjectMapper mapper;

    public RabbitMQConfirmCallback(RabbitMQMessageBackupServices rabbitMQMessageBackupServices, ObjectMapper mapper) {
        this.rabbitMQMessageBackupServices = rabbitMQMessageBackupServices;
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
            RabbitMQMessageBackup messageBackup = rabbitMQMessageBackupServices.getBackupMessage(backupMessageId);

            if (!ack) {
                messageBackup.setMessageStatus(MessageStatus.PENDING);
            } else {
                messageBackup.setMessageStatus(MessageStatus.DELIVERED);
            }
            rabbitMQMessageBackupServices.backupMessage(messageBackup);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
