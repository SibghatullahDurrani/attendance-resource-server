package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.organization.DepartmentOfOrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.organization.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.department.DepartmentService;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import com.main.face_recognition_resource_server.services.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("organizations")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final UserService userService;
    private final DepartmentService departmentService;

    public OrganizationController(OrganizationService organizationService, UserService userService, DepartmentService departmentService) {
        this.organizationService = organizationService;
        this.userService = userService;
        this.departmentService = departmentService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HttpStatus> registerOrganization(@RequestBody RegisterOrganizationDTO organizationToRegister) {
        organizationService.registerOrganization(organizationToRegister);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizationDTO> getOwnOrganization(Authentication authentication) throws UserDoesntExistException {
        OrganizationDTO organization = userService.getOrganizationByUsername(authentication.getName());
        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

    @GetMapping("all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<OrganizationDTO>> getAllOrganizations(@RequestParam int page, @RequestParam int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<OrganizationDTO> organizationPage = organizationService.getAllOrganizations(pageRequest);
        return new ResponseEntity<>(organizationPage, HttpStatus.OK);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable Long id) throws OrganizationDoesntExistException {
        OrganizationDTO organization = organizationService.getOrganizationDTO(id);
        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

//  @GetMapping("departments")
//  @PreAuthorize("hasRole('SUPER_ADMIN')")
//  public ResponseEntity<Page<OrganizationDepartmentDTO>> getAllOrganizationsWithDepartments(@RequestParam int page, @RequestParam int size) {
//    PageRequest pageRequest = PageRequest.of(page, size);
//    Page<OrganizationDepartmentDTO> allOrganizationsWithItsDepartments = organizationServices.getAllOrganizationsWithItsDepartments(pageRequest);
//    return new ResponseEntity<>(allOrganizationsWithItsDepartments, HttpStatus.OK);
//  }

    @GetMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentOfOrganizationDTO>> departmentsOfOrganization(Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<DepartmentOfOrganizationDTO> departmentNamesOfOrganization = departmentService.getDepartmentNamesOfOrganization(organizationId);
        return new ResponseEntity<>(departmentNamesOfOrganization, HttpStatus.OK);
    }
}
