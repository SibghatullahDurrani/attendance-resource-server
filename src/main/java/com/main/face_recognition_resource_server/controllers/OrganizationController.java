package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
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
  private final UserServices userServices;

  public OrganizationController(OrganizationServices organizationServices, UserServices userServices) {
    this.organizationServices = organizationServices;
    this.userServices = userServices;
  }

  @PostMapping()
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerOrganization(@RequestBody RegisterOrganizationDTO organizationToRegister) {
    organizationServices.registerOrganization(organizationToRegister);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<OrganizationDTO> getOwnOrganization(Authentication authentication) {
    OrganizationDTO organization = userServices.getOrganizationByUsername(authentication.getName());
    return new ResponseEntity<>(organization, HttpStatus.OK);
  }

  @GetMapping("all")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Page<OrganizationDTO>> getAllOrganizations(@RequestParam int page, @RequestParam int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<OrganizationDTO> organizationPage = organizationServices.getAllOrganizations(pageRequest);
    return new ResponseEntity<>(organizationPage, HttpStatus.OK);
  }

  @GetMapping("{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable Long id) {
    OrganizationDTO organization = organizationServices.getOrganizationById(id);
    return new ResponseEntity<>(organization, HttpStatus.OK);
  }

  @GetMapping("departments")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Page<OrganizationDepartmentDTO>> getAllOrganizationsWithDepartments(@RequestParam int page, @RequestParam int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<OrganizationDepartmentDTO> allOrganizationsWithItsDepartments = organizationServices.getAllOrganizationsWithItsDepartments(pageRequest);
    return new ResponseEntity<>(allOrganizationsWithItsDepartments, HttpStatus.OK);
  }
}
