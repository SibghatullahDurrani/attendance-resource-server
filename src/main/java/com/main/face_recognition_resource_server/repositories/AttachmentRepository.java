package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
