package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.user.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import com.main.face_recognition_resource_server.utils.DataUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServicesImplTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServicesImpl userServices;

  @Test
  public void getUserDataByUsername_ThrowsUserDoesntExistException() {
    String username = Mockito.anyString();
    when(userRepository.getUserDTOByUsername(username)).thenReturn(Optional.empty());
    assertThrows(UserDoesntExistException.class, () -> userServices.getUserDataByUsername(username));
  }

//  @Test
//  public void getUserDataByUsername_returnsUser() {
//    String username = Mockito.anyString();
//    UserDTO userDTO = UserDTO.builder().build();
//    when(userRepository.getUserDTOByUsername(username)).thenReturn(Optional.of(userDTO));
//    UserDTO user = userServices.getUserDataByUsername(username);
//
//    Assertions.assertThat(user).isSameAs(userDTO);
//  }
//
//  @Test
//  public void registerUser_ThrowsUserAlreadyExistsException() {
//    RegisterUserDTO registerUserDTO = DataUtil.getRegisterUserDTO();
//    when(userServices.userExistsWithEmailAndRole(registerUserDTO.getEmail(), registerUserDTO.getRole())).thenThrow(UserAlreadyExistsException.class);
//    assertThrows(UserAlreadyExistsException.class, () -> userServices.registerUser(registerUserDTO));
//  }

  @Test
  public void userExistsWithEmailAndRole_ThrowsUserAlreadyExistsException() {
    String email = "XYZ";
    UserRole role = UserRole.ROLE_USER;
    when(userRepository.existsByEmailAndRole(email, role)).thenReturn(true);
    assertThrows(UserAlreadyExistsException.class, () -> userServices.userExistsWithEmailAndRole(email, role));
  }

  @Test
  public void getUserOrganizationId_ThrowsUserDoesExistException() {
    String username = Mockito.anyString();
    when(userRepository.getUserOrganizationId(username)).thenReturn(Optional.empty());
    assertThrows(UserDoesntExistException.class, () -> userServices.getUserOrganizationId(username));
  }

//  @Test
//  public void getUserOrganizationId_ReturnsOrganizationId() {
//    String username = Mockito.anyString();
//    Long organizationId = 1L;
//    when(userRepository.getUserOrganizationId(username)).thenReturn(Optional.of(organizationId));
//    Long organizationIdRes = userServices.getUserOrganizationId(username);
//
//    Assertions.assertThat(organizationIdRes).isEqualTo(organizationId);
//  }

  @Test
  public void getDepartmentByUsername_ThrowsUserDoesntExistException() {
    String username = Mockito.anyString();
    when(userRepository.getDepartmentByUsername(username)).thenReturn(Optional.empty());
    assertThrows(UserDoesntExistException.class, () -> userServices.getDepartmentByUsername(username));
  }

//  @Test
//  public void getDepartmentByUsername_ReturnsDepartment() {
//    String username = Mockito.anyString();
//    DepartmentDTO departmentDTO = DataUtil.getDepartmentDTO();
//    when(userRepository.getDepartmentByUsername(username)).thenReturn(Optional.of(departmentDTO));
//    DepartmentDTO department = userServices.getDepartmentByUsername(username);
//    Assertions.assertThat(department).isSameAs(departmentDTO);
//  }

}