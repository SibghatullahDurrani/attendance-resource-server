package com.main.face_recognition_resource_server.services.user;


import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServicesImpl implements UserServices {
  private final UserRepository userRepository;

  public UserServicesImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public ResponseEntity<UserDTO> getOwnUserDataByUsername(String username) {
    Optional<UserDTO> userDTOOptional = userRepository.getOwnDetails(username);
    if (userDTOOptional.isPresent()) {
      UserDTO userDTO = userDTOOptional.get();
      return new ResponseEntity<>(userDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
