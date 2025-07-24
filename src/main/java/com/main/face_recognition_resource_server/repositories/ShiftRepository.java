package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
}
