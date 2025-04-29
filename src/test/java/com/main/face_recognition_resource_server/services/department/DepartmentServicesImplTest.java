package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServicesImplTest {
  @Mock
  private DepartmentRepository departmentRepository;

  @InjectMocks
  private DepartmentServicesImpl departmentServices;

  @Test
  public void departmentExist_ThrowsDepartmentDoesntExistException() {
    Long departmentId = Mockito.anyLong();
    when(departmentRepository.existsById(departmentId)).thenReturn(false);
    assertThrows(DepartmentDoesntExistException.class, () -> departmentServices.departmentExist(departmentId));
  }

  @Test
  public void departmentExist_ReturnsTrue() {
    Long departmentId = Mockito.anyLong();
    when(departmentRepository.existsById(departmentId)).thenReturn(true);
    boolean exists = departmentServices.departmentExist(departmentId);
    Assertions.assertThat(exists).isEqualTo(true);
  }

  @Test
  public void departmentBelongsToOrganization_ThrowsCheckIfDepartmentDoesntExistException() {
    Long departmentId = 1L;
    Long organizationId = 1L;
    when(departmentRepository.getOrganizationIdOfDepartment(departmentId)).thenReturn(Optional.empty());
    assertThrows(DepartmentDoesntExistException.class, () -> departmentServices.checkIfDepartmentBelongsToOrganization(departmentId, organizationId));
  }

  @Test
  public void departmentBelongsToOrganization_ThrowsCheckIfDepartmentDoesntBelongToYourOrganizationException() {
    Long departmentId = 1L;
    Long organizationId = 1L;
    when(departmentRepository.getOrganizationIdOfDepartment(departmentId)).thenReturn(Optional.of(2L));
    assertThrows(DepartmentDoesntBelongToYourOrganizationException.class, () -> departmentServices.checkIfDepartmentBelongsToOrganization(departmentId, organizationId));
  }

  @Test
  public void checkIfDepartmentBelongsToOrganization_ReturnsTrue() {
    Long departmentId = 1L;
    Long organizationId = 1L;
    when(departmentRepository.getOrganizationIdOfDepartment(departmentId)).thenReturn(Optional.of(1L));
    boolean exists = departmentServices.checkIfDepartmentBelongsToOrganization(departmentId, organizationId);
    Assertions.assertThat(exists).isEqualTo(true);
  }
}