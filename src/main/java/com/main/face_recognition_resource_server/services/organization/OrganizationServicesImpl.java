package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.converters.OrganizationToRegisterOrganizationDTOConvertor;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class OrganizationServicesImpl implements OrganizationServices {
  private final OrganizationRepository organizationRepository;

  public OrganizationServicesImpl(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public boolean organizationExists(Long organizationId) throws OrganizationDoesntExistException {
    boolean exists = organizationRepository.existsById(organizationId);
    if (!exists) {
      throw new OrganizationDoesntExistException();
    } else {
      return true;
    }
  }

  @Override
  public void registerOrganization(RegisterOrganizationDTO organizationToRegister) {
    Organization organization = OrganizationToRegisterOrganizationDTOConvertor.convert(organizationToRegister);
    organizationRepository.saveAndFlush(organization);
  }

  @Override
  public Page<OrganizationDTO> getAllOrganizations(Pageable pageable) {
    return organizationRepository.getAllOrganizations(pageable);
  }

  @Override
  public OrganizationDTO getOrganizationDTO(Long id) throws OrganizationDoesntExistException {
    Optional<OrganizationDTO> organizationDTO = organizationRepository.getOrganizationById(id);
    if (organizationDTO.isEmpty()) {
      throw new OrganizationDoesntExistException();
    } else {
      return organizationDTO.get();
    }
  }

  @Override
  public Organization getOrganization(Long id) throws OrganizationDoesntExistException {
    Optional<Organization> organization = organizationRepository.findById(id);
    if (organization.isEmpty()) {
      throw new OrganizationDoesntExistException();
    } else {
      return organization.get();
    }
  }

  @Override
  public Page<OrganizationDepartmentDTO> getAllOrganizationsWithItsDepartments(Pageable pageable) {
    Page<Organization> organizationPage = organizationRepository.getAllOrganizationsWithDepartments(pageable);
    return organizationPage.map(organization -> {
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
  }
}
