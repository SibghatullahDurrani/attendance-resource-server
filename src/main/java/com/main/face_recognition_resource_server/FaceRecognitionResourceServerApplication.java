package com.main.face_recognition_resource_server;

import com.main.face_recognition_resource_server.services.amqp.AmqpService;
import com.main.face_recognition_resource_server.services.attendance.AttendanceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@SpringBootApplication
public class FaceRecognitionResourceServerApplication {
    private final AmqpService amqpService;
    private final AttendanceService attendanceService;

    public FaceRecognitionResourceServerApplication(AmqpService amqpService, AttendanceService attendanceService) {
        this.amqpService = amqpService;
        this.attendanceService = attendanceService;
    }

    public static void main(String[] args) {
        SpringApplication.run(FaceRecognitionResourceServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"));
            amqpService.initQueueAndListener();
            attendanceService.markAbsentOfAllUsersForCurrentDay();
        };
    }
}
