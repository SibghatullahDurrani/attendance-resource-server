package com.main.face_recognition_resource_server.configurations;

import com.main.face_recognition_resource_server.services.amqp.AmqpService;
import org.springframework.stereotype.Component;

@Component
public class CommandLineRunner implements org.springframework.boot.CommandLineRunner {
    private final AmqpService amqpService;

    public CommandLineRunner(AmqpService amqpService) {
        this.amqpService = amqpService;
    }

    @Override
    public void run(String... args) {
        amqpService.initQueueAndListener();
    }
}
