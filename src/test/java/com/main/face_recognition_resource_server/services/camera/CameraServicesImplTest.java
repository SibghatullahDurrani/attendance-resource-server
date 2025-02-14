package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInDepartmentException;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CameraServicesImplTest {
  @Mock
  private CameraRepository cameraRepository;

  @InjectMocks
  private CameraServicesImpl cameraServices;

  @Test
  public void cameraExistsWithIpAddressPortChannelAndDepartment_ThrowsCameraAlreadyExistsInDepartmentException() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    department.setId(1L);
    List<Department> departments = List.of(department);
    assertThrows(CameraAlreadyExistsInDepartmentException.class,
            () -> cameraServices.cameraExistInDepartment(1L, departments));
  }

  @Test
  public void cameraExistInDepartment_ReturnsFalse() {
    Organization organization = DataUtil.getOrganization();
    Department department = DataUtil.getDepartment(organization);
    department.setId(1L);
    List<Department> departments = List.of(department);

    boolean exists = cameraServices.cameraExistInDepartment(0L, departments);
    Assertions.assertThat(exists).isEqualTo(false);
  }
}