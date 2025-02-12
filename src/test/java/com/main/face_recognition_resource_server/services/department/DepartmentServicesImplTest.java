package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServicesImplTest {
  @Mock
  private DepartmentRepository departmentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @InjectMocks
  private DepartmentServicesImpl departmentServices;

  @Test
  public void getDepartmentByUsername_ReturnsHttpStatusOK() {
    String username = "ASDF";
    DepartmentDTO departmentDTO = DataUtil.getDepartmentDTO();
    when(userRepository.getDepartmentByUsername(username)).thenReturn(Optional.of(departmentDTO));
    ResponseEntity<DepartmentDTO> response = departmentServices.getDepartmentByUsername(username);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();
  }

  @Test
  public void getDepartmentByUsername_ReturnsHttpNotFound_WhenUsernameNotValid() {
    String username = Mockito.anyString();
    when(userRepository.getDepartmentByUsername(username)).thenReturn(Optional.empty());
    ResponseEntity<DepartmentDTO> response = departmentServices.getDepartmentByUsername(username);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    Assertions.assertThat(response.getBody()).isNull();
  }

  @Test
  public void registerDepartmentAsSuperAdmin_ReturnsHttpStatusCreated() {
    RegisterDepartmentDTO registerDepartmentDTO = DataUtil.getRegisterDepartmentDTO();
    when(organizationRepository.existsById(registerDepartmentDTO.getOrganizationId())).thenReturn(true);

    ResponseEntity<HttpStatus> response = departmentServices.registerDepartmentAsSuperAdmin(registerDepartmentDTO);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  public void registerDepartmentAsSuperAdmin_ThrowsOrganizationDoesntExistException() {
    RegisterDepartmentDTO registerDepartmentDTO = DataUtil.getRegisterDepartmentDTO();
    when(organizationRepository.existsById(registerDepartmentDTO.getOrganizationId())).thenReturn(false);
    assertThrows(OrganizationDoesntExistException.class, () -> departmentServices.registerDepartmentAsSuperAdmin(registerDepartmentDTO));
  }

  @Test
  public void registerDepartmentAsAdmin_ThrowsOrganizationDoesntExistException() {
    RegisterDepartmentDTO registerDepartmentDTO = DataUtil.getRegisterDepartmentDTO();
    String username = "ASDF";
    when(organizationRepository.existsById(registerDepartmentDTO.getOrganizationId())).thenReturn(false);
    assertThrows(OrganizationDoesntExistException.class, () -> departmentServices.registerDepartmentAsAdmin(registerDepartmentDTO, username));
  }
}