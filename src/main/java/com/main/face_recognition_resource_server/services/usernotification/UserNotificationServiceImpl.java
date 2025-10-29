package com.main.face_recognition_resource_server.services.usernotification;

import com.main.face_recognition_resource_server.DTOS.notification.NotificationCountDTO;
import com.main.face_recognition_resource_server.DTOS.notification.NotificationDTO;
import com.main.face_recognition_resource_server.constants.notification.NotificationStatus;
import com.main.face_recognition_resource_server.domains.Notification;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.domains.UserNotification;
import com.main.face_recognition_resource_server.repositories.UserNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {
    private final UserNotificationRepository userNotificationRepository;

    public UserNotificationServiceImpl(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Override
    public void saveUsersNotification(List<Long> userIds, Notification notification) {
        List<UserNotification> userNotifications = userIds.stream().map(id -> UserNotification.builder()
                .user(User.builder().id(id).build())
                .notification(notification)
                .status(NotificationStatus.NON_READ)
                .build()).toList();

        userNotificationRepository.saveAll(userNotifications);
    }

    @Override
    public NotificationCountDTO getNonReadNotificationCountOfUser(Long userId) {
        return userNotificationRepository.getNonReadNotificationsCountOfUserNotification(userId);
    }

    @Override
    public List<NotificationDTO> getNotificationsOfUser(Long userId) {
        return userNotificationRepository.getNotificationsOfUser(userId);
    }
}
