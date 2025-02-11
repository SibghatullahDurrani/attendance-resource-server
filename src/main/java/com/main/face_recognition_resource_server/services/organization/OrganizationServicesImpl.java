package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.converters.OrganizationToRegisterOrganizationDTOConvertor;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class OrganizationServicesImpl implements OrganizationServices {
  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;

  public OrganizationServicesImpl(OrganizationRepository organizationRepository, UserRepository userRepository) {
    this.organizationRepository = organizationRepository;
    this.userRepository = userRepository;
  }

  @Override
  public ResponseEntity<HttpStatus> registerOrganization(RegisterOrganizationDTO organizationToRegister) {
    Organization organization = OrganizationToRegisterOrganizationDTOConvertor.convert(organizationToRegister);
    organizationRepository.saveAndFlush(organization);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<OrganizationDTO> getOrganizationByUsername(String username) {
    OrganizationDTO organizationDTO = userRepository.getOrganizationByUsername(username);
    return new ResponseEntity<>(organizationDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Page<OrganizationDTO>> getAllOrganizations(Pageable pageable) {
    Page<OrganizationDTO> organizationsDTOs = organizationRepository.getAllOrganizations(pageable);
    return new ResponseEntity<>(organizationsDTOs, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<OrganizationDTO> getOrganizationById(Long id) {
    Optional<OrganizationDTO> optionalOrganization = organizationRepository.getOrganizationById(id);
    return optionalOrganization.map(organizationDTO -> new ResponseEntity<>(organizationDTO, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
