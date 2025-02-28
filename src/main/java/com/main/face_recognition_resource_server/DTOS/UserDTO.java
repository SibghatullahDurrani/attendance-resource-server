package com.main.face_recognition_resource_server.DTOS;

import com.main.face_recognition_resource_server.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDTO {
  private Long id;
  private String firstName;
  private String secondName;
  private String username;
  private UserRole role;
  private String identificationNumber;
  private String email;
  private String departmentName;
  private String organizationName;
  private String profilePicturePath;
}
