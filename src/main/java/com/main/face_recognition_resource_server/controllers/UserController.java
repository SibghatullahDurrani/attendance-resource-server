package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {
  private final UserServices userServices;

  public UserController(UserServices userServices) {
    this.userServices = userServices;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserDTO> getOwnUserData(Authentication authentication) {
    return userServices.getUserDataByUsername(authentication.getName());
  }

  @GetMapping("all-users")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    return userServices.getAllUsers();
  }

  @PostMapping("register-admin")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerAdmin(@RequestBody RegisterUserDTO userToRegister) {
    return userServices.registerAdmin(userToRegister);
  }

  @PostMapping("register-user")
  @PreAuthorize("{hasRole('ADMIN')}")
  public ResponseEntity<HttpStatus> registerUser(@RequestBody RegisterUserDTO userToRegister, Authentication authentication) {
    return userServices.registerUser(userToRegister, authentication.getName());
  }
}
