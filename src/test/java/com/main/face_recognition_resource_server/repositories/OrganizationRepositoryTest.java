package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.constants.OrganizationType;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.utils.AbstractPostgreSQLTestContainer;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

class OrganizationRepositoryTest extends AbstractPostgreSQLTestContainer {

  @Autowired
  private OrganizationRepository organizationRepository;

  @BeforeEach
  void clearDatabase(@Autowired Flyway flyway) {
    flyway.clean();
    flyway.migrate();
  }

  @Test
  public void getAllOrganizations_ReturnsAllOrganizations() {
    Organization organization = DataUtil.getOrganization();
    PageRequest pageRequest = PageRequest.of(1, 10);

    Page<OrganizationDTO> organizations = organizationRepository.getAllOrganizations(pageRequest);

    Assertions.assertThat(organizations.getTotalElements()).isEqualTo(0);

    organizationRepository.saveAndFlush(organization);
    Organization organization2 = Organization.builder().organizationName("ALSDJH").organizationType(OrganizationType.OFFICE).build();
    organizationRepository.saveAndFlush(organization2);

    organizations = organizationRepository.getAllOrganizations(pageRequest);

    Assertions.assertThat(organizations.getTotalElements()).isEqualTo(2);
  }

  @Test
  public void getOrganizationById_ReturnsOrganization() {
    Organization organization = DataUtil.getOrganization();

    Optional<OrganizationDTO> organizationDTO = organizationRepository.getOrganizationById(1L);

    Assertions.assertThat(organizationDTO).isEmpty();

    organizationRepository.saveAndFlush(organization);

    organizationDTO = organizationRepository.getOrganizationById(1L);

    Assertions.assertThat(organizationDTO).isPresent();
  }
}