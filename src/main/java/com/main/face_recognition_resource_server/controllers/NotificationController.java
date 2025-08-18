package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.notification.*;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attachment.AttachmentService;
import com.main.face_recognition_resource_server.services.notification.NotificationService;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.services.usernotification.UserNotificationService;
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
    private final NotificationService notificationService;
    private final UserService userService;
    private final Gson gson;
    private final AttachmentService attachmentService;
    private final UserNotificationService userNotificationService;

    public NotificationController(NotificationService notificationService, UserService userService, AttachmentService attachmentService, UserNotificationService userNotificationService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.attachmentService = attachmentService;
        this.userNotificationService = userNotificationService;
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
                Long attachmentId = attachmentService.saveAttachment(file);
                notificationService.sendNotification(notification, attachmentId);
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        }
        notificationService.sendNotification(notification, null);
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
                Long attachmentId = attachmentService.saveAttachment(file);
                notificationService.sendNotification(notification, attachmentId);
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        }
        notificationService.sendNotification(notification, null);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/organization")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Modifying
    public ResponseEntity<HttpStatus> sendNotificationToUsersOfOrganization(@RequestParam(required = false) MultipartFile file, @RequestParam String data, Authentication authentication) throws UserDoesntExistException, IOException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        SendNotificationToUsersOfOrganizationDTO notification = gson.fromJson(data, SendNotificationToUsersOfOrganizationDTO.class);
        if (file != null) {
            if (!file.isEmpty()) {
                Long attachmentId = attachmentService.saveAttachment(file);
                notificationService.sendNotification(notification, organizationId, attachmentId);
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        }
        notificationService.sendNotification(notification, organizationId, null);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<NotificationDTO>> getOwnNotifications(Authentication authentication) throws UserDoesntExistException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        List<NotificationDTO> notifications = userNotificationService.getNotificationsOfUser(userId);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<NotificationCountDTO> getOwnNonReadNotificationsCount(Authentication authentication) throws UserDoesntExistException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        NotificationCountDTO count = userNotificationService.getNonReadNotificationsCountOfUserNotification(userId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}