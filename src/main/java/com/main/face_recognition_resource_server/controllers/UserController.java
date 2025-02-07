package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  private final UserServices userServices;

  public UserController(UserServices userServices) {
    this.userServices = userServices;
  }

  @GetMapping("user")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserDTO> getOwnUserData(Authentication authentication) {
    return userServices.getOwnUserDataByUsername(authentication.getName());
  }
}
