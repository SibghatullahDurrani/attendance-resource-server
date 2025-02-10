package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  @Query("""
          SELECT d.organization.id FROM Department d WHERE d.id = ?1
          """)
  Optional<Long> getDepartmentOrganizationIdByDepartmentId(Long departmentId);
}