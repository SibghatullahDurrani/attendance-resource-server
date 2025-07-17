package com.main.face_recognition_resource_server.services.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceMessage;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class AmqpServices {
  private final OrganizationServices organizationServices;
  private final String EXHANGE_NAME = "attendance.exchange";
  private final AmqpAdmin amqpAdmin;
  private final ConnectionFactory connectionFactory;
  private final TopicExchange exchange;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AttendanceServices attendanceServices;

  public AmqpServices(OrganizationServices organizationServices, AmqpAdmin amqpAdmin, ConnectionFactory connectionFactory, AttendanceServices attendanceServices) {
    this.organizationServices = organizationServices;
    this.amqpAdmin = amqpAdmin;
    this.connectionFactory = connectionFactory;
    this.attendanceServices = attendanceServices;
    exchange = new TopicExchange(EXHANGE_NAME, true, false);
    amqpAdmin.declareExchange(exchange);
  }

  public void registerQueueAndListener(Long organizationId) {
    String queueName = "attendance." + organizationId + ".queue";
    String routingKey = "attendance." + organizationId + ".key";

    Queue queue = QueueBuilder.durable(queueName).build();
    amqpAdmin.declareQueue(queue);

    Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
    amqpAdmin.declareBinding(binding);

    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queueName);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//    container.setMessageListener(new MessageListenerAdapter(new MessageConsumer(), "handleMessage"));
    container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
      long tag = message.getMessageProperties().getDeliveryTag();
      try {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        AttendanceMessage attendance = objectMapper.readValue(json, AttendanceMessage.class);
        if (attendance.getAttendanceType() == AttendanceType.CHECK_IN) {
          attendanceServices.markCheckIn(attendance.getUserId(), new Date(attendance.getDate()), null, null);
        } else {
          attendanceServices.markCheckOut(attendance.getUserId(), new Date(attendance.getDate()), null, null);
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
    container.start();
    System.out.println("Queue: " + queueName);

  }

  public void initQueueAndListener() {
    List<Long> organizationIds = organizationServices.getAllOrganizationIds();

    for (Long organizationId : organizationIds) {
      registerQueueAndListener(organizationId);
    }
  }
}
