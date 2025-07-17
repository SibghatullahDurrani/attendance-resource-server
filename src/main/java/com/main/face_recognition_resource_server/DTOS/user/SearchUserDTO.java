package com.main.face_recognition_resource_server.DTOS.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchUserDTO {
  private Long id;
  private String name;
  private String departmentName;
}
