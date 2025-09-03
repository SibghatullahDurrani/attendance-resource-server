package com.main.face_recognition_resource_server.DTOS.notification;

import com.main.face_recognition_resource_server.constants.notification.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private NotificationStatus status;
    private String attachmentName;
}
