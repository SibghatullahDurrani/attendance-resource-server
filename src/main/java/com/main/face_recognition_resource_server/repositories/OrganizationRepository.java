package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.leave.LeavesAllowedPolicyDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.domains.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO(
          o.id,o.organizationName,o.organizationType
          ) FROM Organization o
          """)
  Page<OrganizationDTO> getAllOrganizations(Pageable pageable);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO(
          o.id,o.organizationName,o.organizationType
          ) FROM Organization o WHERE o.id = ?1
          """)
  Optional<OrganizationDTO> getOrganizationById(Long id);

  @Query("""
          SELECT o FROM Organization o
          """)
  /*
  HACK: selecting the entire entity and all its data. now it is required
  but in the future if the entity changes and more data is available
  I will have to change this...
  */
  Page<Organization> getAllOrganizationsWithDepartments(Pageable pageable);

  @Query("""
          SELECT o.organizationPolicies.retakeAttendanceInHour FROM Organization o WHERE o.id = ?1
          """)
  int getRetakeAttendanceInHourPolicy(Long organizationId);

  @Query("""
          SELECT o.organizationPolicies.checkInTimeForUser FROM Organization o WHERE o.id = ?1
          """)
  String getCheckInPolicy(Long organizationId);

  @Query("""
          SELECT o.organizationPolicies.lateAttendanceToleranceTimeInMinutes FROM Organization o WHERE o.id = ?1
          """)
  int getLateAttendanceToleranceTimePolicy(Long organizationId);

  @Query("""
          SELECT o.organizationPolicies.checkOutTimeForUser FROM Organization o WHERE o.id = ?1
          """)
  String getCheckOutPolicy(Long organizationId);

  @Query("""
          SELECT o.organizationPolicies.checkOutToleranceTimeInHour FROM Organization o WHERE o.id = ?1
          """)
  int getCheckOutToleranceTimePolicy(Long organizationId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.leave.LeavesAllowedPolicyDTO(
          o.organizationPolicies.sickLeavesAllowed, o.organizationPolicies.annualLeavesAllowed
          ) FROM Organization o WHERE o.id = ?1
          """)
  LeavesAllowedPolicyDTO getOrganizationLeavesAllowedPolicies(Long organizationId);

}
