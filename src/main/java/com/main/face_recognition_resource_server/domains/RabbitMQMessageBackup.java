package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.rabbitmq.MessageStatus;
import com.main.face_recognition_resource_server.constants.rabbitmq.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "rabbitmq_message_backups")
public class RabbitMQMessageBackup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus messageStatus;

    @Column(nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;
}
