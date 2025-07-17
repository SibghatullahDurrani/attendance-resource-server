package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.notification.NotificationCountDTO;
import com.main.face_recognition_resource_server.DTOS.notification.NotificationDTO;
import com.main.face_recognition_resource_server.domains.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
  @Query("""
          SELECT un.notification.id FROM UserNotification un WHERE un.user.id = ?1
          """)
  List<Long> getNotificationIdsOfUserNotifications(Long userId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.notification.NotificationCountDTO(
              COUNT(un) FILTER(WHERE un.user.id = ?1 AND un.status = "NON_READ")
          )  FROM UserNotification un
          """)
  NotificationCountDTO getNonReadNotificationsCountOfUserNotification(Long userId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.notification.NotificationDTO(
                    un.notification.id, un.notification.title, un.notification.message,un.status, att.fileName
          )
          FROM UserNotification un
          LEFT JOIN un.notification.attachment att
          WHERE un.user.id = ?1
          """)
  List<NotificationDTO> getNotificationsOfUser(Long userId);
}
