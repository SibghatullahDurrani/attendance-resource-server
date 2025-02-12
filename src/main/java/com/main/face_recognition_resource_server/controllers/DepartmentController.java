package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.services.department.DepartmentServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("departments")
public class DepartmentController {
  private final DepartmentServices departmentServices;

  public DepartmentController(DepartmentServices departmentServices) {
    this.departmentServices = departmentServices;
  }

  @GetMapping()
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DepartmentDTO> getOwnDepartment(Authentication authentication) {
    return departmentServices.getDepartmentByUsername(authentication.getName());
  }

  @PostMapping()
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
  public ResponseEntity<HttpStatus> registerDepartment(@RequestBody RegisterDepartmentDTO departmentToRegister, Authentication authentication) {
    boolean isSuperAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(UserRole.ROLE_SUPER_ADMIN.toString()));
    if (isSuperAdmin) {
      return departmentServices.registerDepartmentAsSuperAdmin(departmentToRegister);
    } else {
      return departmentServices.registerDepartmentAsAdmin(departmentToRegister, authentication.getName());
    }
  }

}
