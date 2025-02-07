package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserServices {
  ResponseEntity<UserDTO> getUserDataByUsername(String username);

  ResponseEntity<List<UserDTO>> getAllUsers();

  ResponseEntity<HttpStatus> registerAdmin(RegisterUserDTO userToRegister);

  ResponseEntity<HttpStatus> registerUser(RegisterUserDTO userToRegister);
}
