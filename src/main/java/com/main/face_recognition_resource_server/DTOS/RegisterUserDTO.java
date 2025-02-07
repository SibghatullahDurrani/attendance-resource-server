package com.main.face_recognition_resource_server.DTOS;

import lombok.Data;

@Data
public class RegisterUserDTO {
  private String firstName;
  private String secondName;
  private String password;
  private String identificationNumber;
  private String email;
  private Long departmentId;
}