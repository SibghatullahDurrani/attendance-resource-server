package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface OrganizationServices {
  ResponseEntity<HttpStatus> registerOrganization(RegisterOrganizationDTO organizationToRegister);
}
