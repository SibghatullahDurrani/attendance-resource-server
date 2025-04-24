package com.main.face_recognition_resource_server.DTOS.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepartmentOfOrganizationDTO {
  private Long id;
  private String departmentName;
}
