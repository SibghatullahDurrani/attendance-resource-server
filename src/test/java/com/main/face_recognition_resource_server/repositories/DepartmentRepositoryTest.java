package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.utils.AbstractPostgreSQLTestContainer;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class DepartmentRepositoryTest extends AbstractPostgreSQLTestContainer {
  @Autowired
  private DepartmentRepository departmentRepository;

  @Autowired
  private OrganizationRepository organizationRepository;


  @BeforeEach
  void clearDatabase(@Autowired Flyway flyway) {
    flyway.clean();
    flyway.migrate();
  }

  @Test
  public void getDepartmentOrganizationIdByDepartmentId_ReturnOrganizationId_IfExists() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);

    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);

    Optional<Long> departmentId = departmentRepository.getDepartmentOrganizationIdByDepartmentId(department.getId());

    Assertions.assertThat(departmentId).isPresent();
    Assertions.assertThat(departmentId.get()).isEqualTo(organization.getId());
  }

  @Test
  public void getDepartmentOrganizationIdByDepartmentId_ReturnNothing_IFNotExists() {
    Optional<Long> departmentId = departmentRepository.getDepartmentOrganizationIdByDepartmentId(1L);

    Assertions.assertThat(departmentId).isEmpty();
  }
}