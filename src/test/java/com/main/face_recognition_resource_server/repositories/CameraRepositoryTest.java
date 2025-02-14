package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import com.main.face_recognition_resource_server.domains.Camera;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.utils.AbstractPostgreSQLTestContainer;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

class CameraRepositoryTest extends AbstractPostgreSQLTestContainer {
  @Autowired
  private DepartmentRepository departmentRepository;
  @Autowired
  private OrganizationRepository organizationRepository;
  @Autowired
  private CameraRepository cameraRepository;

  @BeforeEach
  public void clearDatabase(@Autowired Flyway flyway) {
    flyway.clean();
    flyway.migrate();
  }

  @Test
  public void getCamerasOfDepartment_ReturnsValidData() {
    Organization organization = DataUtil.getOrganization();
    organizationRepository.saveAndFlush(organization);

    Department department1 = DataUtil.getDepartment(organization);
    departmentRepository.saveAndFlush(department1);
    Department department2 = DataUtil.getDepartment(organization);
    departmentRepository.saveAndFlush(department2);

    List<Department> departments1 = List.of(department1);

    Camera camera1 = DataUtil.getCamera(departments1);
    cameraRepository.saveAndFlush(camera1);
    Camera camera2 = DataUtil.getCamera(departments1);
    cameraRepository.saveAndFlush(camera2);

    List<DepartmentCameraDTO> camerasOfDepartment = cameraRepository.getCamerasOfDepartment(1L);
    List<DepartmentCameraDTO> camerasOfDepartment2 = cameraRepository.getCamerasOfDepartment(2L);

    Assertions.assertThat(camerasOfDepartment2.size()).isEqualTo(0);
    Assertions.assertThat(camerasOfDepartment.size()).isEqualTo(2);
    Assertions.assertThat(camerasOfDepartment.get(0).getId()).isBetween(1L, 2L);
    Assertions.assertThat(camerasOfDepartment.get(1).getId()).isBetween(1L, 2L);
  }

  @Test
  public void existsByIpAddressAndPortAndChannelAndDepartments_ReturnTrue() {
    Organization organization = DataUtil.getOrganization();
    organizationRepository.saveAndFlush(organization);

    Department department1 = DataUtil.getDepartment(organization);
    departmentRepository.saveAndFlush(department1);
    List<Department> departments1 = List.of(department1);
    Camera camera1 = DataUtil.getCamera(departments1);
    cameraRepository.saveAndFlush(camera1);

    boolean exists = cameraRepository.existsByIpAddressAndPortAndChannelAndDepartmentId(camera1.getIpAddress(), camera1.getPort(), camera1.getChannel(), department1.getId());

    Assertions.assertThat(exists).isEqualTo(true);
  }

  @Test
  public void existsByIpAddressAndPortAndChannelAndDepartments_ReturnFalse() {
    Organization organization = DataUtil.getOrganization();
    organizationRepository.saveAndFlush(organization);

    Department department1 = DataUtil.getDepartment(organization);
    departmentRepository.saveAndFlush(department1);
    List<Department> departments1 = List.of(department1);
    Camera camera1 = DataUtil.getCamera(departments1);
    cameraRepository.saveAndFlush(camera1);

    boolean exists = cameraRepository.existsByIpAddressAndPortAndChannelAndDepartmentId(camera1.getIpAddress(), camera1.getPort(), camera1.getChannel(), 2L);

    Assertions.assertThat(exists).isEqualTo(false);
  }

  @Test
  public void getCameraByIpAddressPortChannelAndType_ReturnsValidCamera() {
    Organization organization = DataUtil.getOrganization();
    organizationRepository.saveAndFlush(organization);
    Department department = DataUtil.getDepartment(organization);
    departmentRepository.saveAndFlush(department);
    List<Department> departments = List.of(department);
    Camera camera = DataUtil.getCamera(departments);

    cameraRepository.saveAndFlush(camera);
    Optional<Camera> camera1 = cameraRepository.getCameraByIpAddressPortAndChannel(
            camera.getIpAddress(),
            camera.getPort(),
            camera.getChannel()
    );
    Optional<Camera> camera2 = cameraRepository.getCameraByIpAddressPortAndChannel(
            camera.getIpAddress(),
            camera.getPort(),
            1
    );

    Assertions.assertThat(camera1).isPresent();
    Assertions.assertThat(camera2).isEmpty();
  }
}