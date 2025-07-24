package com.main.face_recognition_resource_server.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessage;
import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import com.main.face_recognition_resource_server.utilities.ControlRoutingKey;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Objects;

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

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) {
                return;
            }
            try {
                if (Objects.requireNonNull(correlationData.getReturned()).getExchange().equals(CONTROL_EXCHANGE_NAME)) {
                    ControlRoutingKey controlRoutingKey = new ControlRoutingKey(correlationData.getReturned().getRoutingKey());
                    switch (controlRoutingKey.getControlRoutingType()) {
                        case SHIFT:
                            handleShiftConfirmCallback(correlationData.getReturned().getMessage().getBody(), ack);
                            break;
                        case USER:
                            break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            byte[] message = returned.getMessage().getBody();
            String exchangeName = returned.getExchange();
            String routingKey = returned.getRoutingKey();
            try {
                if (exchangeName.equals(CONTROL_EXCHANGE_NAME)) {
                    ControlRoutingKey controlRoutingKey = new ControlRoutingKey(routingKey);
                    switch (controlRoutingKey.getControlRoutingType()) {
                        case SHIFT:
                            handleShiftReturnsCallBack(message);
                            break;
                        case USER:
                            break;
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return rabbitTemplate;
    }

    private void handleShiftConfirmCallback(byte[] message, boolean isAcknowledged) throws IOException {
        ShiftMessage shiftMessage = objectMapper.readValue(message, ShiftMessage.class);
        RabbitMQMessageBackup messageBackup = RabbitMQMessageBackup.builder()
                .id(shiftMessage.getMessageBackupId())
                .build();

        if (!isAcknowledged) {
            messageBackup.setMessageStatus(MessageStatus.PENDING);
        } else {
            messageBackup.setMessageStatus(MessageStatus.DELIVERED);
        }

        rabbitMQMessageBackupServices.backupAndReturnMessage(messageBackup);
    }

    private void handleShiftReturnsCallBack(byte[] message) throws IOException {
        ShiftMessage shiftMessage = objectMapper.readValue(message, ShiftMessage.class);
        RabbitMQMessageBackup messageBackup = RabbitMQMessageBackup.builder()
                .id(shiftMessage.getMessageBackupId())
                .messageStatus(MessageStatus.PENDING)
                .build();
        rabbitMQMessageBackupServices.backupAndReturnMessage(messageBackup);
    }
}
