package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;
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
    PageRequest pageRequest = PageRequest.of(1, 10);
    UserDTO userDTO = UserDTO.builder().build();
    when(userRepository.getAllUsers(pageRequest)).thenReturn(new PageImpl<>(List.of(userDTO)));
    ResponseEntity<Page<UserDTO>> allUsers = userServices.getAllUsers(pageRequest);

    Assertions.assertThat(allUsers.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(Objects.requireNonNull(allUsers.getBody()).getTotalElements()).isEqualTo(1);
  }

  @Test
  public void getAllUsers_ReturnsHttpNotFound_WhenThereAreNoUsers() {
    PageRequest pageRequest = PageRequest.of(1, 10);
    when(userRepository.getAllUsers(pageRequest)).thenReturn(Page.empty());
    ResponseEntity<Page<UserDTO>> allUsers = userServices.getAllUsers(pageRequest);

    Assertions.assertThat(allUsers.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void registerAdmin_throwsDepartmentNotFoundException_WhenDepartmentDoesntExist() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.existsById(registerUser.getDepartmentId())).thenReturn(false);
    assertThrows(DepartmentDoesntExistException.class, () -> userServices.registerAdmin(registerUser));
  }

  @Test
  public void registerAdmin_ThrowsUserAlreadyExistsException_WhenUserAlreadyExists() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .email("")
            .build();
    when(departmentRepository.existsById(registerUser.getDepartmentId())).thenReturn(true);
    when(userRepository.existsByEmailAndRole(registerUser.getEmail(), UserRole.ROLE_ADMIN)).thenReturn(true);
    assertThrows(UserAlreadyExistsException.class, () -> userServices.registerAdmin(registerUser));

  }

  @Test
  public void registerAdmin_ReturnsHttpCreated_WhenDepartmentExists() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .email("")
            .build();
    when(departmentRepository.existsById(registerUser.getDepartmentId())).thenReturn(true);
    when(userRepository.existsByEmailAndRole(registerUser.getEmail(), UserRole.ROLE_ADMIN)).thenReturn(false);
    ResponseEntity<HttpStatus> registerAdminResponse = userServices.registerAdmin(registerUser);

    Assertions.assertThat(registerAdminResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  public void registerUser_ThrowsDepartmentNotFoundException_WhenDepartmentDoesntExist() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.getOrganizationIdOfDepartment(registerUser.getDepartmentId())).thenReturn(Optional.empty());
    assertThrows(DepartmentDoesntExistException.class, () -> userServices.registerUser(registerUser, Mockito.anyString()));
  }

  @Test
  public void registerUser_ThrowsDepartmentDoesntBelongToYourOrganizationException() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .build();
    when(departmentRepository.getOrganizationIdOfDepartment(registerUser.getDepartmentId())).thenReturn(Optional.of(1L));
    when(userRepository.getUserOrganizationId(Mockito.anyString())).thenReturn(2L);
    assertThrows(DepartmentDoesntBelongToYourOrganizationException.class, () -> userServices.registerUser(registerUser, Mockito.anyString()));
  }

  @Test
  public void registerUser_ThrowsUserAlreadyExistsException() {
    RegisterUserDTO registerUser = RegisterUserDTO.builder()
            .departmentId(Mockito.anyLong())
            .email("")
            .build();
    when(departmentRepository.getOrganizationIdOfDepartment(registerUser.getDepartmentId())).thenReturn(Optional.of(1L));
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
    when(departmentRepository.getOrganizationIdOfDepartment(registerUser.getDepartmentId())).thenReturn(Optional.of(1L));
    when(userRepository.getUserOrganizationId(Mockito.anyString())).thenReturn(1L);
    when(userRepository.existsByEmailAndRole(registerUser.getEmail(), UserRole.ROLE_USER)).thenReturn(false);

    ResponseEntity<HttpStatus> registerUserResponse = userServices.registerUser(registerUser, Mockito.anyString());

    Assertions.assertThat(registerUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }
}