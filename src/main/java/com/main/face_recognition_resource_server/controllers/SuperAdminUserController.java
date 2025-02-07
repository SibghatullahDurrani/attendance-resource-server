package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SuperAdminUserController {
  private final UserServices userServices;

  public SuperAdminUserController(UserServices userServices) {
    this.userServices = userServices;
  }

  @GetMapping("user/all-users")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<UserDTO>> getAllUsers(){
    return userServices.getAllUsers();
  }
}
