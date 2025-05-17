package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.department.DepartmentsTableRecordDTO;
import com.main.face_recognition_resource_server.DTOS.department.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.department.DepartmentServices;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("departments")
public class DepartmentController {
  private final DepartmentServices departmentServices;
  private final UserServices userServices;
  private final OrganizationServices organizationServices;

  public DepartmentController(DepartmentServices departmentServices, UserServices userServices, OrganizationServices organizationServices) {
    this.departmentServices = departmentServices;
    this.userServices = userServices;
    this.organizationServices = organizationServices;
  }

  @GetMapping()
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DepartmentDTO> getOwnDepartment(Authentication authentication) throws UserDoesntExistException {
    DepartmentDTO department = userServices.getDepartmentByUsername(authentication.getName());
    return new ResponseEntity<>(department, HttpStatus.OK);
  }

//  @PostMapping()
//  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
//  public ResponseEntity<HttpStatus> registerDepartment(@RequestBody RegisterDepartmentDTO departmentToRegister, Authentication authentication) throws
//          OrganizationDoesntExistException,
//          UserDoesntExistException,
//          OrganizationDoesntBelongToYouException {
//    boolean isSuperAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(UserRole.ROLE_SUPER_ADMIN.toString()));
//    boolean organizationExists = organizationServices.organizationExists(departmentToRegister.getOrganizationId());
//    if (organizationExists) {
//      if (isSuperAdmin) {
//        departmentServices.registerDepartment(departmentToRegister);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//      } else {
//        Long userOrganizationId = userServices.getUserOrganizationId(authentication.getName());
//        if (!userOrganizationId.equals(departmentToRegister.getOrganizationId())) {
//          throw new OrganizationDoesntBelongToYouException();
//        } else {
//          departmentServices.registerDepartment(departmentToRegister);
//          return new ResponseEntity<>(HttpStatus.CREATED);
//        }
//      }
//    }
//    return null;
//  }

  @GetMapping("/departments-table")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<DepartmentsTableRecordDTO>> getDepartmentsTableData(@RequestParam int page, @RequestParam int size, Authentication authentication) throws UserDoesntExistException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<DepartmentsTableRecordDTO> departmentsTable = departmentServices.getDepartmentsTableData(organizationId, pageRequest);
    return new ResponseEntity<>(departmentsTable, HttpStatus.OK);
  }

  @PostMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<HttpStatus> registerDepartments(@RequestBody List<RegisterDepartmentDTO> departmentsToRegister, Authentication authentication) throws UserDoesntExistException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    departmentServices.registerDepartments(departmentsToRegister, organizationId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
