package com.main.face_recognition_resource_server.DTOS.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDepartmentDTO {
  private String departmentName;
  private Long organizationId;
}
