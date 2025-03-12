package com.main.face_recognition_resource_server.converters;

import com.main.face_recognition_resource_server.DTOS.organization.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Organization;

public class OrganizationToRegisterOrganizationDTOConvertor {
  public static Organization convert(RegisterOrganizationDTO registerOrganizationDTO) {
    return Organization.builder()
            .organizationName(registerOrganizationDTO.getOrganizationName())
            .organizationType(registerOrganizationDTO.getOrganizationType())
            .build();
  }
}
