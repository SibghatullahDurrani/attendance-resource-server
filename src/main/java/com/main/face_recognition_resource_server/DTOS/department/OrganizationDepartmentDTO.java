package com.main.face_recognition_resource_server.DTOS.department;

import com.main.face_recognition_resource_server.constants.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrganizationDepartmentDTO {
  private Long organizationId;
  private String organizationName;
  private OrganizationType organizationType;
  private List<DepartmentDTO> departments;
}
