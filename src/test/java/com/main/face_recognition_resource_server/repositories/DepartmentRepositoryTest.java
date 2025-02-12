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
  public void getDepartmentOrganizationIdById_ReturnOrganizationId_OfDepartment_IfExists() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);

    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);

    Optional<Long> departmentId = departmentRepository.getOrganizationIdOfDepartment(department.getId());

    Assertions.assertThat(departmentId).isPresent();
    Assertions.assertThat(departmentId.get()).isEqualTo(organization.getId());
  }

  @Test
  public void getOrganizationId_OfDepartment_ReturnNothing_IFNotExists() {
    Optional<Long> departmentId = departmentRepository.getOrganizationIdOfDepartment(1L);

    Assertions.assertThat(departmentId).isEmpty();
  }

  @Test
  public void registerDepartment_RegistersDepartment() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);

    Optional<Department> departmentOptional = departmentRepository.findById(1L);

    Assertions.assertThat(departmentOptional).isEmpty();

    organizationRepository.saveAndFlush(organization);

    departmentRepository.registerDepartment(department.getDepartmentName(), 1L);

    departmentOptional = departmentRepository.findById(1L);

    Assertions.assertThat(departmentOptional).isPresent();
  }
}