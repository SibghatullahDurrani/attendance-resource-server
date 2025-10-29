package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.department.DepartmentsTableRecordDTO;
import com.main.face_recognition_resource_server.DTOS.department.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.exceptions.DepartmentAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.department.DepartmentService;
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
@RequestMapping("departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final UserService userService;

    public DepartmentController(DepartmentService departmentService, UserService userService) {
        this.departmentService = departmentService;
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DepartmentDTO> getOwnDepartment(Authentication authentication) throws UserDoesntExistException {
        DepartmentDTO department = userService.getDepartmentByUsername(authentication.getName());
        return new ResponseEntity<>(department, HttpStatus.OK);
    }

    @GetMapping("/organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DepartmentsTableRecordDTO>> organizationDepartments(@RequestParam int page, @RequestParam int size, Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<DepartmentsTableRecordDTO> departmentsTable = departmentService.getOrganizationDepartments(organizationId, pageRequest);
        return new ResponseEntity<>(departmentsTable, HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> registerDepartments(@RequestBody List<RegisterDepartmentDTO> departmentsToRegister, Authentication authentication) throws UserDoesntExistException, DepartmentAlreadyExistsException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        departmentService.registerDepartments(departmentsToRegister, organizationId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
