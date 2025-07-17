package com.main.face_recognition_resource_server.services.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceMessage;
import org.springframework.amqp.core.Message;

import java.io.IOException;

public class MessageConsumer {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public void handleMessage(String message) {
    try {
      AttendanceMessage attendanceMessage = objectMapper.readValue(message, AttendanceMessage.class);
      System.out.println(attendanceMessage.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
