package com.main.face_recognition_resource_server.services.notification;

import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersOfDepartmentsDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersOfOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Attachment;
import com.main.face_recognition_resource_server.domains.Notification;
import com.main.face_recognition_resource_server.repositories.NotificationRepository;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.services.usernotification.UserNotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserNotificationService userNotificationService;
    private final UserService userService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserNotificationService userNotificationService, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userNotificationService = userNotificationService;
        this.userService = userService;
    }

    @Override
    public void sendNotification(SendNotificationToUsersDTO notificationToSend, Long attachmentId) {
        Notification notification = Notification.builder()
                .title(notificationToSend.getTitle())
                .message(notificationToSend.getMessage())
                .build();

        if (attachmentId != null) {
            notification.setAttachment(Attachment.builder().id(attachmentId).build());
        }

        Notification notificationSaved = notificationRepository.saveAndFlush(notification);
        userNotificationService.saveUsersNotification(notificationToSend.getIntendedUserIds(), notificationSaved);
    }

    @Override
    public void sendNotification(SendNotificationToUsersOfDepartmentsDTO notification, Long attachmentId) {
        List<Long> userIds = userService.getAllUserIdsOfDepartments(notification.getDepartmentIds());
        SendNotificationToUsersDTO notificationToSend = SendNotificationToUsersDTO.builder()
                .title(notification.getTitle())
                .message(notification.getMessage())
                .intendedUserIds(userIds)
                .build();
        sendNotification(notificationToSend, attachmentId);
    }

    @Override
    public void sendNotification(SendNotificationToUsersOfOrganizationDTO notification, Long organizationId, Long attachmentId) {
        List<Long> userIds = userService.getAllUserIdsOfOrganization(organizationId);
        SendNotificationToUsersDTO notificationToSend = SendNotificationToUsersDTO.builder()
                .title(notification.getTitle())
                .message(notification.getMessage())
                .intendedUserIds(userIds)
                .build();
        sendNotification(notificationToSend, attachmentId);
    }

}
