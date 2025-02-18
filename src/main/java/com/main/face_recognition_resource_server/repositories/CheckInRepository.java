package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
}
