package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrganizationServices {
  ResponseEntity<HttpStatus> registerOrganization(RegisterOrganizationDTO organizationToRegister);

  ResponseEntity<OrganizationDTO> getOrganizationByUsername(String username);

  ResponseEntity<List<OrganizationDTO>> getAllOrganizations();
}
