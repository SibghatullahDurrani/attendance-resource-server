package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.UserDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserServices {
  ResponseEntity<UserDTO> getOwnUserDataByUsername(String username);

  ResponseEntity<List<UserDTO>> getAllUsers();
}
