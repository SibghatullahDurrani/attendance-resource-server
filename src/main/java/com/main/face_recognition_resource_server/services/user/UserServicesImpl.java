package com.main.face_recognition_resource_server.services.user;


import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentNotFoundException;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServicesImpl implements UserServices {
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServicesImpl(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.departmentRepository = departmentRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public ResponseEntity<UserDTO> getUserDataByUsername(String username) {
    Optional<UserDTO> userDTOOptional = userRepository.getUserByUsername(username);
    if (userDTOOptional.isPresent()) {
      UserDTO userDTO = userDTOOptional.get();
      return new ResponseEntity<>(userDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
    Page<UserDTO> allUsers = userRepository.getAllUsers(pageable);
    if (allUsers.getTotalElements() == 0) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(allUsers, HttpStatus.OK);
  }

  @Override
  @Transactional
  public ResponseEntity<HttpStatus> registerAdmin(RegisterUserDTO userToRegister) {
    boolean doesDepartmentExist = departmentRepository.existsById(userToRegister.getDepartmentId());
    if (!doesDepartmentExist) {
      throw new DepartmentNotFoundException();
    }
    boolean doesUserExistsWithEmail = userRepository.existsByEmailAndRole(userToRegister.getEmail(), UserRole.ROLE_ADMIN);
    if (doesUserExistsWithEmail) {
      throw new UserAlreadyExistsException("Admin account has already been made with this email");
    } else {
      registerUser(userToRegister, UserRole.ROLE_ADMIN);
      return new ResponseEntity<>(HttpStatus.CREATED);
    }
  }

  @Override
  @Transactional
  public ResponseEntity<HttpStatus> registerUser(RegisterUserDTO userToRegister, String adminUsername) {
    Optional<Long> organizationId = departmentRepository.getDepartmentOrganizationIdByDepartmentId(userToRegister.getDepartmentId());
    if (organizationId.isEmpty()) {
      throw new DepartmentNotFoundException();
    }
    Long adminOrganizationId = userRepository.getUserOrganizationId(adminUsername);
    if (!organizationId.get().equals(adminOrganizationId)) {
      throw new DepartmentDoesntBelongToYourOrganizationException();
    }
    boolean doesUserExistWithEmail = userRepository.existsByEmailAndRole(userToRegister.getEmail(), UserRole.ROLE_USER);
    if (doesUserExistWithEmail) {
      throw new UserAlreadyExistsException("User account has already been made with this email");
    } else {
      registerUser(userToRegister, UserRole.ROLE_USER);
      return new ResponseEntity<>(HttpStatus.CREATED);
    }
  }

  private void registerUser(RegisterUserDTO userToRegister, UserRole role) {
    String hashedPassword = passwordEncoder.encode(userToRegister.getPassword());
    Long usernameSequence = userRepository.nextUsernameSequence();
    String username = userToRegister.getFirstName() + userToRegister.getSecondName() + "#" + usernameSequence;
    userRepository.registerUser(
            userToRegister.getFirstName(),
            userToRegister.getSecondName(),
            hashedPassword,
            username,
            role.toString(),
            userToRegister.getIdentificationNumber(),
            userToRegister.getEmail(),
            userToRegister.getDepartmentId()
    );
  }
}