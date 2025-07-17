package com.main.face_recognition_resource_server.services.notification;

import com.main.face_recognition_resource_server.DTOS.notification.NotificationDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersOfDepartmentsDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersOfOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Attachment;
import com.main.face_recognition_resource_server.domains.Notification;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.domains.UserNotification;
import com.main.face_recognition_resource_server.repositories.AttachmentRepository;
import com.main.face_recognition_resource_server.repositories.NotificationRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import com.main.face_recognition_resource_server.services.usernotification.UserNotificationServices;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServicesImpl implements NotificationServices {
    private final NotificationRepository notificationRepository;
    private final UserNotificationServices userNotificationServices;
    private final UserServices userServices;

    public NotificationServicesImpl(NotificationRepository notificationRepository, UserNotificationServices userNotificationServices, UserServices userServices) {
        this.notificationRepository = notificationRepository;
        this.userNotificationServices = userNotificationServices;
        this.userServices = userServices;
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
        userNotificationServices.saveUsersNotification(notificationToSend.getIntendedUserIds(), notificationSaved);
    }

    @Override
    public void sendNotification(SendNotificationToUsersOfDepartmentsDTO notification, Long attachmentId) {
        List<Long> userIds = userServices.getAllUserIdsOfDepartments(notification.getDepartmentIds());
        SendNotificationToUsersDTO notificationToSend = SendNotificationToUsersDTO.builder()
                .title(notification.getTitle())
                .message(notification.getMessage())
                .intendedUserIds(userIds)
                .build();
        sendNotification(notificationToSend, attachmentId);
    }

    @Override
    public void sendNotification(SendNotificationToUsersOfOrganizationDTO notification, Long organizationId, Long attachmentId) {
        List<Long> userIds = userServices.getAllUserIdsOfOrganization(organizationId);
        SendNotificationToUsersDTO notificationToSend = SendNotificationToUsersDTO.builder()
                .title(notification.getTitle())
                .message(notification.getMessage())
                .intendedUserIds(userIds)
                .build();
        sendNotification(notificationToSend, attachmentId);
    }

}
