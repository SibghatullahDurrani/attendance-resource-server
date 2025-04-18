package com.main.face_recognition_resource_server.DTOS.user;

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
  private String department;
  private long organizationId;
  private UserRole role;
  private String profilePictureName;
  private String sourceFacePictureName;
}