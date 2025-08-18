package com.main.face_recognition_resource_server.configurations.rabbitmq;

import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitMQReturnsCallback implements RabbitTemplate.ReturnsCallback {
    private final RabbitMQMessageBackupService rabbitMQMessageBackupService;

    public RabbitMQReturnsCallback(RabbitMQMessageBackupService rabbitMQMessageBackupService) {
        this.rabbitMQMessageBackupService = rabbitMQMessageBackupService;
    }


    @Override
    public void returnedMessage(ReturnedMessage returned) {
        Message message = returned.getMessage();
        MessageProperties messageProperties = message.getMessageProperties();
        UUID backupMessageId = UUID.fromString(messageProperties.getHeader("uuid"));
        RabbitMQMessageBackup messageBackup = rabbitMQMessageBackupService.getBackupMessage(backupMessageId);

        messageBackup.setMessageStatus(MessageStatus.PENDING);

        rabbitMQMessageBackupService.backupMessage(messageBackup);
    }
}
