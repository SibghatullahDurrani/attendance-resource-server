package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentNotFoundException;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServicesImplTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private DepartmentRepository departmentRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServicesImpl userServices;

  @Test
  public void getUserDataByUsername_ReturnsHttpOkAndUserDTO_WhenUserIsPresent() {
    String username = Mockito.anyString();
    UserDTO userDTO = UserDTO.builder().build();
    when(userRepository.getUserByUsername(username)).thenReturn(Optional.of(userDTO));
    ResponseEntity<UserDTO> userDataByUsername = userServices.getUserDataByUsername(username);

    Assertions.assertThat(userDataByUsername.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(userDataByUsername.getBody()).isNotNull();
  }

  @Test
  public void getUserDataByUsername_ReturnsHttpNotFound_WhenUserIsNotPresent() {
    when(userRepository.getUserByUsername(Mockito.anyString())).thenReturn(Optional.empty());
    ResponseEntity<UserDTO> userDataByUsername = userServices.getUserDataByUsername(Mockito.anyString());

    Assertions.assertThat(userDataByUsername.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void getAllUsers_ReturnsHttpOK_WhenThereAreUsers() {
    UserDTO userDTO = UserDTO.builder().build();
    when(userRepository.getAllUsers()).thenReturn(List.of(userDTO));
    ResponseEntity<List<UserDTO>> allUsers = userServices.getAllUsers();

    Assertions.assertThat(allUsers.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(allUsers.getBody()).isNotEmpty();
  }

  @Test
  public void getAllUsers_ReturnsHttpNotFound_WhenThereAreNoUsers() {
    when(userRepository.getAllUsers()).thenReturn(List.of());
    ResponseEntity<List<UserDTO>> allUsers = userServices.getAllUsers();

    Assertions.assertThat(allUsers.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void registerAdmin_throwsDepartmentNotFoundException_WhenDepartmentDoesntExist() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.existsById(registerUser.getDepartmentId())).thenReturn(false);
    assertThrows(DepartmentNotFoundException.class, () -> userServices.registerAdmin(registerUser));
  }

  @Test
  public void registerAdmin_ReturnsHttpCreated_WhenDepartmentExists() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.existsById(registerUser.getDepartmentId())).thenReturn(true);
    ResponseEntity<HttpStatus> registerAdminResponse = userServices.registerAdmin(registerUser);

    Assertions.assertThat(registerAdminResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  public void registerUser_ThrowsDepartmentNotFoundException_WhenDepartmentDoesntExist() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.getDepartmentOrganizationIdByDepartmentId(registerUser.getDepartmentId())).thenReturn(Optional.empty());
    assertThrows(DepartmentNotFoundException.class, () -> userServices.registerUser(registerUser, Mockito.anyString()));
  }

  @Test
  public void registerUser_ThrowsDepartmentDoesntBelongToYourOrganizationException() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.getDepartmentOrganizationIdByDepartmentId(registerUser.getDepartmentId())).thenReturn(Optional.of(1L));
    when(userRepository.getUserOrganizationId(Mockito.anyString())).thenReturn(2L);
    assertThrows(DepartmentDoesntBelongToYourOrganizationException.class, () -> userServices.registerUser(registerUser, Mockito.anyString()));
  }

  @Test
  public void registerUser_ThrowsUserAlreadyExistsException() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .email("")
            .build();
    when(departmentRepository.getDepartmentOrganizationIdByDepartmentId(registerUser.getDepartmentId())).thenReturn(Optional.of(1L));
    when(userRepository.getUserOrganizationId(Mockito.anyString())).thenReturn(1L);
    when(userRepository.existsByEmailAndRole(registerUser.getEmail(), UserRole.ROLE_USER)).thenReturn(true);
    assertThrows(UserAlreadyExistsException.class, () -> userServices.registerUser(registerUser, Mockito.anyString()));
  }

  @Test
  public void registerUser_ReturnsHttpCreated() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .email("")
            .build();
    when(departmentRepository.getDepartmentOrganizationIdByDepartmentId(registerUser.getDepartmentId())).thenReturn(Optional.of(1L));
    when(userRepository.getUserOrganizationId(Mockito.anyString())).thenReturn(1L);
    when(userRepository.existsByEmailAndRole(registerUser.getEmail(), UserRole.ROLE_USER)).thenReturn(false);

    ResponseEntity<HttpStatus> registerUserResponse = userServices.registerUser(registerUser, Mockito.anyString());

    Assertions.assertThat(registerUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }
}