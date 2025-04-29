package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.user.AdminUsersTableRecordDTO;
import com.main.face_recognition_resource_server.DTOS.user.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDataDTO;
import com.main.face_recognition_resource_server.exceptions.*;
import com.main.face_recognition_resource_server.services.department.DepartmentServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;


@RestController
@RequestMapping("users")
public class UserController {
  private final UserServices userServices;
  private final DepartmentServices departmentServices;

  public UserController(UserServices userServices, DepartmentServices departmentServices) {
    this.userServices = userServices;
    this.departmentServices = departmentServices;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserDTO> getOwnUserData(Authentication authentication) throws UserDoesntExistException {
    UserDTO user = userServices.getUserDataByUsername(authentication.getName());
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @GetMapping("all")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Page<UserDTO>> getAllUsers(@RequestParam int page, @RequestParam int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<UserDTO> usersPage = userServices.getAllUsers(pageRequest);
    return new ResponseEntity<>(usersPage, HttpStatus.OK);
  }

  @GetMapping("organization/{organizationId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AdminUsersTableRecordDTO>> getUsersPageOfOrganization(@PathVariable Long organizationId, @RequestParam int page, @RequestParam int size, Authentication authentication) throws OrganizationDoesntBelongToYouException, UserDoesntExistException {
    userServices.checkIfOrganizationBelongsToUser(organizationId, authentication.getName());
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<AdminUsersTableRecordDTO> adminUsersTableRecordDTOPage = userServices.getUsersPageOfOrganization(organizationId, pageRequest);
    return new ResponseEntity<>(adminUsersTableRecordDTOPage, HttpStatus.OK);
  }

  @PostMapping()
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ResponseEntity<HttpStatus> registerUser(@RequestBody RegisterUserDTO userToRegister, Authentication authentication)
          throws DepartmentDoesntExistException,
          DepartmentDoesntBelongToYourOrganizationException,
          UserAlreadyExistsException,
          SQLException,
          IOException,
          UserDoesntExistException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    departmentServices.checkIfDepartmentBelongsToOrganization(userToRegister.getDepartmentId(), organizationId);
    userServices.registerUser(userToRegister, organizationId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDataDTO> getUserData(@PathVariable Long userId, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    userServices.checkIfOrganizationBelongsToUser(userId, organizationId);
    UserDataDTO userData = userServices.getUserData(userId);
    return new ResponseEntity<>(userData, HttpStatus.OK);
  }
}
