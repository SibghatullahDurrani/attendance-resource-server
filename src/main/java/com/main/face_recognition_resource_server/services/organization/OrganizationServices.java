package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationServices {
  boolean organizationExists(Long organizationId) throws OrganizationDoesntExistException;

  void registerOrganization(RegisterOrganizationDTO organizationToRegister);

  Page<OrganizationDTO> getAllOrganizations(Pageable pageable);

  OrganizationDTO getOrganizationById(Long id) throws OrganizationDoesntExistException;

  Page<OrganizationDepartmentDTO> getAllOrganizationsWithItsDepartments(Pageable pageable);
}
