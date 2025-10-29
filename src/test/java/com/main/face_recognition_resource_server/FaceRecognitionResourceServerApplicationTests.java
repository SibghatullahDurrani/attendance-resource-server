package com.main.face_recognition_resource_server;

import com.main.face_recognition_resource_server.services.amqp.AmqpService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class FaceRecognitionResourceServerApplicationTests {
    @MockitoBean
    private AmqpService amqpService;

    @Test
    void contextLoads() {
    }

}
