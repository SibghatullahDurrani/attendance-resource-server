package com.main.face_recognition_resource_server.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import com.main.face_recognition_resource_server.utilities.MessageMetadataWrapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class RabbitMQConfigurations {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RabbitMQMessageBackupServices rabbitMQMessageBackupServices;
    private final String CONTROL_EXCHANGE_NAME = "control.exchange";

    public RabbitMQConfigurations(RabbitMQMessageBackupServices rabbitMQMessageBackupServices) {
        this.rabbitMQMessageBackupServices = rabbitMQMessageBackupServices;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });

        rabbitTemplate.setConfirmCallback((correlationData, ack, _) -> {
            if (correlationData == null) {
                return;
            }
            try {
                MessageMetadataWrapper metaDataWrapper = objectMapper.readValue(correlationData.getId(), MessageMetadataWrapper.class);
                UUID backupMessageId = objectMapper.convertValue(metaDataWrapper.backupMessageId(), UUID.class);
                RabbitMQMessageBackup messageBackup = RabbitMQMessageBackup.builder()
                        .id(backupMessageId)
                        .build();

                if (!ack) {
                    messageBackup.setMessageStatus(MessageStatus.PENDING);
                } else {
                    messageBackup.setMessageStatus(MessageStatus.DELIVERED);
                }
                rabbitMQMessageBackupServices.backupMessage(messageBackup);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            Message message = returned.getMessage();
            MessageProperties messageProperties = message.getMessageProperties();
            UUID backupMessageId = UUID.fromString(messageProperties.getHeader("uuid"));
            RabbitMQMessageBackup messageBackup = RabbitMQMessageBackup.builder()
                    .id(backupMessageId)
                    .messageStatus(MessageStatus.PENDING)
                    .build();
            rabbitMQMessageBackupServices.backupMessage(messageBackup);

        });
        return rabbitTemplate;
    }

}
