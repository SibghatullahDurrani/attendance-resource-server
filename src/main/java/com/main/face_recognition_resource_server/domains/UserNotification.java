package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.notification.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "users_notifications")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_user_id_generator")
    @SequenceGenerator(name = "notification_user_id_generator", sequenceName = "notification_user_id_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
}

