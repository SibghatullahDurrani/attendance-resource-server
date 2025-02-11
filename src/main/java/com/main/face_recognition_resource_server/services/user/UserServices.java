package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface UserServices {
  ResponseEntity<UserDTO> getUserDataByUsername(String username);

  ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable);

  ResponseEntity<HttpStatus> registerAdmin(RegisterUserDTO userToRegister);

  ResponseEntity<HttpStatus> registerUser(RegisterUserDTO userToRegister, String adminUsername);
}