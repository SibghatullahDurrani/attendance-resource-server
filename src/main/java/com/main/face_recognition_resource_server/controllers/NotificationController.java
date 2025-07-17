package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.notification.*;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attachment.AttachmentServices;
import com.main.face_recognition_resource_server.services.notification.NotificationServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import com.main.face_recognition_resource_server.services.usernotification.UserNotificationServices;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("notifications")
public class NotificationController {
  private final NotificationServices notificationServices;
  private final UserServices userServices;
  private final Gson gson;
  private final AttachmentServices attachmentServices;
  private final UserNotificationServices userNotificationServices;

  public NotificationController(NotificationServices notificationServices, UserServices userServices, AttachmentServices attachmentServices, UserNotificationServices userNotificationServices) {
    this.notificationServices = notificationServices;
    this.userServices = userServices;
    this.attachmentServices = attachmentServices;
    this.userNotificationServices = userNotificationServices;
    gson = new Gson();
  }

  @PostMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Modifying
  public ResponseEntity<HttpStatus> sendNotificationToUsers(@RequestParam(required = false) MultipartFile file, @RequestParam String data) throws IOException {
    SendNotificationToUsersDTO notification = gson.fromJson(data, SendNotificationToUsersDTO.class);
    if (file != null) {
      if (!file.isEmpty()) {
        Long attachmentId = attachmentServices.saveAttachment(file);
        notificationServices.sendNotification(notification, attachmentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
      }
    }
    notificationServices.sendNotification(notification, null);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/departments")
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Modifying
  public ResponseEntity<HttpStatus> sendNotificationToUsersOfDepartments(@RequestParam(required = false) MultipartFile file, @RequestParam String data) throws IOException {
    SendNotificationToUsersOfDepartmentsDTO notification = gson.fromJson(data, SendNotificationToUsersOfDepartmentsDTO.class);
    if (file != null) {
      if (!file.isEmpty()) {
        Long attachmentId = attachmentServices.saveAttachment(file);
        notificationServices.sendNotification(notification, attachmentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
      }
    }
    notificationServices.sendNotification(notification, null);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/organization")
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Modifying
  public ResponseEntity<HttpStatus> sendNotificationToUsersOfOrganization(@RequestParam(required = false) MultipartFile file, @RequestParam String data, Authentication authentication) throws UserDoesntExistException, IOException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    SendNotificationToUsersOfOrganizationDTO notification = gson.fromJson(data, SendNotificationToUsersOfOrganizationDTO.class);
    if (file != null) {
      if (!file.isEmpty()) {
        Long attachmentId = attachmentServices.saveAttachment(file);
        notificationServices.sendNotification(notification, organizationId, attachmentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
      }
    }
    notificationServices.sendNotification(notification, organizationId, null);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<NotificationDTO>> getOwnNotifications(Authentication authentication) throws UserDoesntExistException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    List<NotificationDTO> notifications = userNotificationServices.getNotificationsOfUser(userId);
    return new ResponseEntity<>(notifications, HttpStatus.OK);
  }

  @GetMapping("/count")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<NotificationCountDTO> getOwnNonReadNotificationsCount(Authentication authentication) throws UserDoesntExistException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    NotificationCountDTO count = userNotificationServices.getNonReadNotificationsCountOfUserNotification(userId);
    return new ResponseEntity<>(count, HttpStatus.OK);
  }
}