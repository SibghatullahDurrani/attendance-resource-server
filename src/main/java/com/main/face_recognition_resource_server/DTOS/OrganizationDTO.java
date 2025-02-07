package com.main.face_recognition_resource_server.DTOS;

import com.main.face_recognition_resource_server.constants.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrganizationDTO {
  private Long id;
  private String oganizationName;
  private OrganizationType organizationType;
}
