package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.user.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserServices {
  UserDTO getUserDataByUsername(String username) throws UserDoesntExistException;

  Page<UserDTO> getAllUsers(Pageable pageable);

  void registerUser(RegisterUserDTO userToRegister) throws UserAlreadyExistsException;

  boolean userExistsWithEmailAndRole(String email, UserRole role) throws UserAlreadyExistsException;

  Long getUserOrganizationId(String username) throws UserDoesntExistException;

  DepartmentDTO getDepartmentByUsername(String username) throws UserDoesntExistException;

  OrganizationDTO getOrganizationByUsername(String username) throws UserDoesntExistException;

  Long getUserDepartmentId(String username) throws UserDoesntExistException;

  User getUserById(Long userId) throws UserDoesntExistException;

  boolean userExistsWithUserId(Long userId);

  Long getUserOrganizationIdByUserId(Long userId) throws UserDoesntExistException;

  List<User> getUsersByOrganizationId(Long organizationId);

  Long getUserIdByUsername(String name) throws UserDoesntExistException;

  User getUserByUsername(String username) throws UserDoesntExistException;

  User saveUser(User user);

  RemainingLeavesDTO getRemainingLeavesOfUser(String username);
}