package com.main.face_recognition_resource_server.DTOS.organization;

import com.main.face_recognition_resource_server.constants.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
  private Long id;
  private String organizationName;
  private OrganizationType organizationType;
}
