package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.utils.AbstractPostgreSQLTestContainer;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class UserRepositoryTest extends AbstractPostgreSQLTestContainer {
  @Autowired
  private UserRepository userRepository;

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
  public void registerUser_RegistersUser() {
    Organization organization = DataUtil.getOrganization();
    organizationRepository.saveAndFlush(organization);
    Department department = DataUtil.getDepartment(organization);

    departmentRepository.saveAndFlush(department);
    userRepository.registerUser(
            "XYZ",
            "XYZ",
            "XYZ",
            "XYZ",
            UserRole.ROLE_USER.toString(),
            "XYZ",
            "XYZ",
            1L
    );

    Optional<User> user = userRepository.findById(1L);

    Assertions.assertThat(user).isPresent();
    Assertions.assertThat(user.get().getDepartment().getId()).isEqualTo(1L);
  }

  @Test
  public void getUserByUsername_ReturnsUser() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);
    User user = DataUtil.getUser(department);

    userRepository.saveAndFlush(user);

    Optional<UserDTO> userByUsername = userRepository.getUserByUsername(user.getUsername());

    Assertions.assertThat(userByUsername).isPresent();
    Assertions.assertThat(userByUsername.get().getUsername()).isEqualTo(user.getUsername());
  }

  @Test
  public void getUserByUsername_ReturnEmpty_WhenNotExist() {
    Optional<UserDTO> userByUsername = userRepository.getUserByUsername("XYZ");

    Assertions.assertThat(userByUsername).isEmpty();
  }

  @Test
  public void getOrganizationByUsername_ReturnOrganization() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);
    User user = DataUtil.getUser(department);

    userRepository.saveAndFlush(user);

    Optional<OrganizationDTO> organizationByUsername = userRepository.getOrganizationByUsername(user.getUsername());

    Assertions.assertThat(organizationByUsername).isPresent();
    Assertions.assertThat(organizationByUsername.get().getOrganizationName()).isEqualTo(organization.getOrganizationName());
  }

  @Test
  public void getUserOrganizationId_ReturnOrganizationId() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);
    User user = DataUtil.getUser(department);

    userRepository.saveAndFlush(user);
    Long organizationId = userRepository.getUserOrganizationId(user.getUsername());

    Assertions.assertThat(organizationId).isEqualTo(1L);
  }

  @Test
  public void existsByEmailAndRole_ReturnTrue_IfExists() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);
    User user = DataUtil.getUser(department);

    userRepository.saveAndFlush(user);
    boolean exists = userRepository.existsByEmailAndRole(user.getEmail(), user.getRole());

    Assertions.assertThat(exists).isEqualTo(true);
  }

  @Test
  public void existsByEmailAndRole_ReturnFalse_IfNotExists() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);
    User user = DataUtil.getUser(department);

    userRepository.saveAndFlush(user);
    boolean exists = userRepository.existsByEmailAndRole(user.getEmail(), UserRole.ROLE_SUPER_ADMIN);

    Assertions.assertThat(exists).isEqualTo(false);
  }

  @Test
  public void getDepartmentByUsername_returnsDepartment() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    organizationRepository.saveAndFlush(organization);
    departmentRepository.saveAndFlush(department);
    User user = DataUtil.getUser(department);

    userRepository.saveAndFlush(user);
    Optional<DepartmentDTO> departmentDTO = userRepository.getDepartmentByUsername(user.getUsername());

    Assertions.assertThat(departmentDTO).isNotEmpty();
    Assertions.assertThat(departmentDTO.get().getDepartmentName()).isEqualTo(department.getDepartmentName());
  }
}