package com.main.face_recognition_resource_server.DTOS.rabbitmqmessagebackup;

import com.main.face_recognition_resource_server.constants.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RabbitMQMessageBackupDTO {
    private UUID id;
    private String message;
    private MessageStatus status;
}
