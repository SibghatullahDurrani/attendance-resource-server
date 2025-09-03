package com.main.face_recognition_resource_server.services.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceMessage;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.services.attendance.AttendanceService;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class AmqpService {
    private final OrganizationService organizationService;
    private final AmqpAdmin amqpAdmin;
    private final ConnectionFactory connectionFactory;
    private final TopicExchange attendance_exchange;
    private final TopicExchange control_exchange;
    private final ObjectMapper objectMapper;
    private final AttendanceService attendanceService;

    public AmqpService(OrganizationService organizationService, AmqpAdmin amqpAdmin, ConnectionFactory connectionFactory, ObjectMapper objectMapper, AttendanceService attendanceService) {
        this.organizationService = organizationService;
        this.amqpAdmin = amqpAdmin;
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.attendanceService = attendanceService;

        String ATTENDANCE_EXCHANGE_NAME = "attendance.exchange";
        String CONTROL_EXCHANGE_NAME = "control.exchange";

        attendance_exchange = new TopicExchange(ATTENDANCE_EXCHANGE_NAME, true, false);
        control_exchange = new TopicExchange(CONTROL_EXCHANGE_NAME, true, false);
        amqpAdmin.declareExchange(attendance_exchange);
        amqpAdmin.declareExchange(control_exchange);
    }

    public void registerQueueAndListener(Long organizationId) {
        String attendanceQueueName = "attendance." + organizationId + ".queue";
        String attendanceRoutingKey = "attendance." + organizationId + ".key";
        Queue attendanceQueue = QueueBuilder.durable(attendanceQueueName).build();
        amqpAdmin.declareQueue(attendanceQueue);
        Binding attendanceBinding = BindingBuilder.bind(attendanceQueue).to(attendance_exchange).with(attendanceRoutingKey);
        amqpAdmin.declareBinding(attendanceBinding);

        String userControlQueueName = "control." + organizationId + ".user.queue";
        String userControlRoutingKey = "control." + organizationId + ".user.key";
        Queue userControlQueue = QueueBuilder.durable(userControlQueueName).build();
        amqpAdmin.declareQueue(userControlQueue);
        Binding userControlBinding = BindingBuilder.bind(userControlQueue).to(control_exchange).with(userControlRoutingKey);
        amqpAdmin.declareBinding(userControlBinding);

        String shiftControlQueueName = "control." + organizationId + ".shift.queue";
        String shiftControlRoutingKey = "attendance." + organizationId + ".shift.key";
        Queue shiftControlQueue = QueueBuilder.durable(shiftControlQueueName).build();
        amqpAdmin.declareQueue(shiftControlQueue);
        Binding shiftControlBinding = BindingBuilder.bind(shiftControlQueue).to(control_exchange).with(shiftControlRoutingKey);
        amqpAdmin.declareBinding(shiftControlBinding);

        SimpleMessageListenerContainer attendanceMessageListenerContainer = attendanceMessageListenerContainer(attendanceQueueName);
        attendanceMessageListenerContainer.start();
    }

    private SimpleMessageListenerContainer attendanceMessageListenerContainer(String attendanceQueueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(attendanceQueueName);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            long tag = message.getMessageProperties().getDeliveryTag();
            try {
                String json = new String(message.getBody(), StandardCharsets.UTF_8);
                AttendanceMessage attendance = objectMapper.readValue(json, AttendanceMessage.class);
                if (attendance.getAttendanceType() == AttendanceType.CHECK_IN) {
                    attendanceService.markCheckIn(attendance.getUserId(), new Date(attendance.getDate()), null, null);
                } else {
                    attendanceService.markCheckOut(attendance.getUserId(), new Date(attendance.getDate()), null, null);
                }
                System.out.println("✅ " + attendance);

                if (channel != null) {
                    channel.basicAck(tag, false);
                }
            } catch (Exception e) {
                System.err.println("❌ Failed: " + e.getMessage());
                if (channel != null) {
                    channel.basicNack(tag, false, true); // requeue
                }
            }
        });
        return container;
    }

    public void initQueueAndListener() {
        List<Long> organizationIds = organizationService.getAllOrganizationIds();

        for (Long organizationId : organizationIds) {
            registerQueueAndListener(organizationId);
        }
    }
}
