package com.main.face_recognition_resource_server.services.user;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserServices {
  UserDTO getUserDataByUsername(String username) throws UserDoesntExistException;

  Page<UserDTO> getAllUsers(Pageable pageable);

  void registerUser(RegisterUserDTO userToRegister);

  boolean userExistsWithEmailAndRole(String email, UserRole role) throws UserAlreadyExistsException;

  Long getUserOrganizationId(String username) throws UserDoesntExistException;

  DepartmentDTO getDepartmentByUsername(String username) throws UserDoesntExistException;

  OrganizationDTO getOrganizationByUsername(String username) throws UserDoesntExistException;

  Long getUserDepartmentId(String username) throws UserDoesntExistException;
}