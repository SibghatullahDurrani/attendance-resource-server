package com.main.face_recognition_resource_server.services.notification;

import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersOfDepartmentsDTO;
import com.main.face_recognition_resource_server.DTOS.notification.SendNotificationToUsersOfOrganizationDTO;


public interface NotificationServices {
  void sendNotification(SendNotificationToUsersDTO notification, Long attachmentId);

  void sendNotification(SendNotificationToUsersOfDepartmentsDTO notification, Long attachmentId);

  void sendNotification(SendNotificationToUsersOfOrganizationDTO notification, Long organizationId, Long attachmentId);

}
