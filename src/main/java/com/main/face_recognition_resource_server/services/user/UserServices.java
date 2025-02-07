package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.UserDTO;
import org.springframework.http.ResponseEntity;

public interface UserServices {
  ResponseEntity<UserDTO> getOwnUserDataByUsername(String username);
}
