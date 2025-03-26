package com.main.face_recognition_resource_server.DTOS.organization;

import com.main.face_recognition_resource_server.constants.OrganizationType;
import lombok.Data;

@Data
public class RegisterOrganizationDTO {
  private String organizationName;
  private OrganizationType organizationType;
  private RegisterOrganizationPoliciesDTO organizationPolicies;
}