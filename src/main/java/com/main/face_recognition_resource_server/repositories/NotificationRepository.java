package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
