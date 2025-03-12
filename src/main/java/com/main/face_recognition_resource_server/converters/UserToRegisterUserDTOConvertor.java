package com.main.face_recognition_resource_server.converters;

import com.main.face_recognition_resource_server.DTOS.user.RegisterUserDTO;
import com.main.face_recognition_resource_server.domains.User;

public class UserToRegisterUserDTOConvertor {
  public static User convert(RegisterUserDTO registerUserDTO) {
    return User.builder()
            .firstName(registerUserDTO.getFirstName())
            .secondName(registerUserDTO.getSecondName())
            .password(registerUserDTO.getPassword())
            .identificationNumber(registerUserDTO.getIdentificationNumber())
            .email(registerUserDTO.getEmail())
            .build();

  }
}
