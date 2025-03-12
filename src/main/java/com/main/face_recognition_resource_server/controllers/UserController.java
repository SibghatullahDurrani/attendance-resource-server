package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.user.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.department.DepartmentServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

  @PostMapping()
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ResponseEntity<HttpStatus> registerUser(@RequestBody RegisterUserDTO userToRegister, Authentication authentication)
          throws DepartmentDoesntExistException,
          UserDoesntExistException,
          DepartmentDoesntBelongToYourOrganizationException,
          UserAlreadyExistsException {
    boolean isSuperAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(UserRole.ROLE_SUPER_ADMIN.toString()));
    boolean departmentExists = departmentServices.departmentExist(userToRegister.getDepartmentId());
    if (departmentExists) {
      if (isSuperAdmin) {
        userServices.registerUser(userToRegister);
        return new ResponseEntity<>(HttpStatus.CREATED);
      } else {
        Long userOrganizationId = userServices.getUserOrganizationId(authentication.getName());
        if (departmentServices.departmentBelongsToOrganization(userToRegister.getDepartmentId(), userOrganizationId)) {
          return new ResponseEntity<>(HttpStatus.CREATED);
        }
      }
    }
    return null;
  }
}
