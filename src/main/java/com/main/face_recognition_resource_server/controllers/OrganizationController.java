package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("organizations")
public class OrganizationController {
  private final OrganizationServices organizationServices;

  public OrganizationController(OrganizationServices organizationServices) {
    this.organizationServices = organizationServices;
  }

  @PostMapping("register-organization")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerOrganization(@RequestBody RegisterOrganizationDTO organizationToRegister) {
    return organizationServices.registerOrganization(organizationToRegister);
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<OrganizationDTO> getOwnOrganization(Authentication authentication) {
    return organizationServices.getOrganizationByUsername(authentication.getName());
  }

  @GetMapping("all-organizations")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Page<OrganizationDTO>> getAllOrganizations(@RequestParam int page, @RequestParam int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    return organizationServices.getAllOrganizations(pageRequest);
  }

  @GetMapping("{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable Long id) {
    return organizationServices.getOrganizationById(id);
  }

  @GetMapping("departments")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Page<OrganizationDepartmentDTO>> getAllOrganizationsWithDepartments(@RequestParam int page, @RequestParam int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    return organizationServices.getAllOrganizationsWithItsDepartments(pageRequest);
  }

}
