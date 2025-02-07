package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.converters.OrganizationToRegisterOrganizationDTOConvertor;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServicesImpl implements OrganizationServices {
  private final OrganizationRepository organizationRepository;

  public OrganizationServicesImpl(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public ResponseEntity<HttpStatus> registerOrganization(RegisterOrganizationDTO organizationToRegister) {
    Organization organization = OrganizationToRegisterOrganizationDTOConvertor.convert(organizationToRegister);
    organizationRepository.saveAndFlush(organization);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
