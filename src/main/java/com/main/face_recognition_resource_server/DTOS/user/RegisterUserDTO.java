package com.main.face_recognition_resource_server.DTOS.user;

import com.main.face_recognition_resource_server.constants.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserDTO {
  private String firstName;
  private String secondName;
  private String password;
  private String identificationNumber;
  private String phoneNumber;
  private String email;
  private Long departmentId;
  private Long designation;
  private UserRole role;
}