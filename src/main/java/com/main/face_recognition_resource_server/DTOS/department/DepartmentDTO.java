package com.main.face_recognition_resource_server.DTOS.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DepartmentDTO {
  private Long id;
  private String departmentName;
}
