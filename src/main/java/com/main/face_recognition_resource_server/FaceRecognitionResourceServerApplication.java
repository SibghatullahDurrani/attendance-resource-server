package com.main.face_recognition_resource_server;

import com.main.face_recognition_resource_server.services.amqp.AmqpServices;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@SpringBootApplication
public class FaceRecognitionResourceServerApplication {
  private final AmqpServices amqpServices;
  private final AttendanceServices attendanceServices;

  public FaceRecognitionResourceServerApplication(AmqpServices amqpServices, AttendanceServices attendanceServices) {
    this.amqpServices = amqpServices;
    this.attendanceServices = attendanceServices;
  }

  public static void main(String[] args) {
    SpringApplication.run(FaceRecognitionResourceServerApplication.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner() {
    return args -> {
      TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"));
      amqpServices.initQueueAndListener();
      attendanceServices.markAbsentOfAllUsersForCurrentDay();
    };
  }
}
