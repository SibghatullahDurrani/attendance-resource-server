package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RabbitMQMessageBackupRepository extends JpaRepository<RabbitMQMessageBackup, UUID> {
}
