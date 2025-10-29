package com.main.face_recognition_resource_server.services.usernotification;

import com.main.face_recognition_resource_server.DTOS.notification.NotificationCountDTO;
import com.main.face_recognition_resource_server.DTOS.notification.NotificationDTO;
import com.main.face_recognition_resource_server.domains.Notification;

import java.util.List;

public interface UserNotificationService {
    void saveUsersNotification(List<Long> userIds, Notification notification);

    NotificationCountDTO getNonReadNotificationCountOfUser(Long userId);

    List<NotificationDTO> getNotificationsOfUser(Long userId);
}
