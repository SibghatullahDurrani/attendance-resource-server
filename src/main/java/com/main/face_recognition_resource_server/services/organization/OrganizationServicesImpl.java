package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.converters.OrganizationToRegisterOrganizationDTOConvertor;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
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
    Optional<OrganizationDTO> optionalOrganization = userRepository.getOrganizationByUsername(username);
    return optionalOrganization.map(organizationDTO -> new ResponseEntity<>(organizationDTO, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
  }

  @Override
  public ResponseEntity<Page<OrganizationDTO>> getAllOrganizations(Pageable pageable) {
    Page<OrganizationDTO> organizationDTOS = organizationRepository.getAllOrganizations(pageable);
    return new ResponseEntity<>(organizationDTOS, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<OrganizationDTO> getOrganizationById(Long id) {
    Optional<OrganizationDTO> optionalOrganization = organizationRepository.getOrganizationById(id);
    return optionalOrganization.map(organizationDTO -> new ResponseEntity<>(organizationDTO, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Override
  public ResponseEntity<Page<OrganizationDepartmentDTO>> getAllOrganizationsWithItsDepartments(Pageable pageable) {
    Page<Organization> organizationPage = organizationRepository.getAllOrganizationsWithDepartments(pageable);
    Page<OrganizationDepartmentDTO> organizationDepartmentDTOPage = organizationPage.map(organization -> {
      List<DepartmentDTO> departments = organization.getDepartments().stream()
              .map(department -> new DepartmentDTO(
                      department.getId(),
                      department.getDepartmentName())
              ).toList();
      return new OrganizationDepartmentDTO(
              organization.getId(),
              organization.getOrganizationName(),
              organization.getOrganizationType(),
              departments
      );
    });
    return new ResponseEntity<>(organizationDepartmentDTOPage, HttpStatus.OK);
  }
}
