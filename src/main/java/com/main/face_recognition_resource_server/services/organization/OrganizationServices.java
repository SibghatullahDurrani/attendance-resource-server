package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.department.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.organization.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationServices {
  boolean organizationExists(Long organizationId) throws OrganizationDoesntExistException;

  void registerOrganization(RegisterOrganizationDTO organizationToRegister);

  Page<OrganizationDTO> getAllOrganizations(Pageable pageable);

  OrganizationDTO getOrganizationDTO(Long id) throws OrganizationDoesntExistException;

  Organization getOrganization(Long id) throws OrganizationDoesntExistException;

  Page<OrganizationDepartmentDTO> getAllOrganizationsWithItsDepartments(Pageable pageable);

  String attendanceRetakeTimingCron(Long organizationId);

  String getOrganizationCheckInPolicy(Long organizationId);

  int getOrganizationLateAttendanceToleranceTimePolicy(Long organizationId);
}
