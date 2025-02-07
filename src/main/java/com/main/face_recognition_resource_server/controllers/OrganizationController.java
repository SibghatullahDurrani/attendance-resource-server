package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("organizations")
public class OrganizationController {
  private final OrganizationServices organizationServices;

  public OrganizationController(OrganizationServices organizationServices) {
    this.organizationServices = organizationServices;
  }

  @PostMapping("register-organization")
  public ResponseEntity<HttpStatus> registerOrganization(@RequestBody RegisterOrganizationDTO organizationToRegister) {
    return organizationServices.registerOrganization(organizationToRegister);
  }
}
