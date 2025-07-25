package com.main.face_recognition_resource_server.repositories.shift;

import com.main.face_recognition_resource_server.domains.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShiftRepository extends JpaRepository<Shift, Long>, JpaSpecificationExecutor<Shift>, ShiftCriteriaRepository {
}
