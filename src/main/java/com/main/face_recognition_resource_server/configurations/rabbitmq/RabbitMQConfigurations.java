package com.main.face_recognition_resource_server.configurations.rabbitmq;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigurations {
    private final RabbitMQConfirmCallback rabbitMQConfirmCallback;
    private final RabbitMQReturnsCallback rabbitMQReturnsCallback;

    public RabbitMQConfigurations(RabbitMQConfirmCallback rabbitMQConfirmCallback, RabbitMQReturnsCallback rabbitMQReturnsCallback) {
        this.rabbitMQConfirmCallback = rabbitMQConfirmCallback;
        this.rabbitMQReturnsCallback = rabbitMQReturnsCallback;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });

        rabbitTemplate.setConfirmCallback(rabbitMQConfirmCallback);
        rabbitTemplate.setReturnsCallback(rabbitMQReturnsCallback);
        return rabbitTemplate;
    }

}
