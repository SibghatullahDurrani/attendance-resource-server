package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.organization.DepartmentOfOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  @Query("""
          SELECT d.organization.id FROM Department d WHERE d.id = ?1
          """)
  Optional<Long> getOrganizationIdOfDepartment(Long departmentId);

  @Query(value = "INSERT INTO departments (department_name, organization_id) VALUES (?1, ?2)", nativeQuery = true)
  @Transactional
  @Modifying
  void registerDepartment(String departmentName, Long organizationId);

  @Query("""
          SELECT d.id FROM Department d WHERE d.organization.id = ?1
          """)
  List<Long> getDepartmentIdsOfOrganization(Long organizationId);

  @Query("""
          SELECT d.departmentName FROM Department d WHERE d.id = ?1
          """)
  Optional<String> getDepartmentName(Long departmentId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.organization.DepartmentOfOrganizationDTO(
          d.id, d.departmentName
          )
          FROM Department d WHERE d.organization.id = ?1
          """)
  List<DepartmentOfOrganizationDTO> getDepartmentNamesOfOrganization(Long organizationId);
}