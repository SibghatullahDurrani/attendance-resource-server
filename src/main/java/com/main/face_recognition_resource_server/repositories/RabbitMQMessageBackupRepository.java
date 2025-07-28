package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RabbitMQMessageBackupRepository extends JpaRepository<RabbitMQMessageBackup, UUID> {
    @Query("""
            SELECT m FROM RabbitMQMessageBackup m WHERE m.messageStatus='PENDING'
            """)
    List<RabbitMQMessageBackup> getPendingMessages();

    @Query("""
            SELECT m.id FROM RabbitMQMessageBackup m WHERE m.messageStatus="DELIVERED"
            """)
    List<UUID> getAllDeliveredMessagesIds();
}
