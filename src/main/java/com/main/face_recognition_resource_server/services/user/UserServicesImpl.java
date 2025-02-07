package com.main.face_recognition_resource_server.services.user;


import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.converters.UserToRegisterUserDTOConverter;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
  public ResponseEntity<UserDTO> getOwnUserDataByUsername(String username) {
    Optional<UserDTO> userDTOOptional = userRepository.getOwnDetails(username);
    if (userDTOOptional.isPresent()) {
      UserDTO userDTO = userDTOOptional.get();
      return new ResponseEntity<>(userDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> allUsers = userRepository.getAllUserDetails();
    if (allUsers.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(allUsers, HttpStatus.OK);
  }

  @Override
  @Transactional
  public ResponseEntity<HttpStatus> registerAdmin(RegisterUserDTO userToRegister) {
    Optional<Department> optionalDepartment = departmentRepository.getDepartmentById(userToRegister.getDepartmentId());
    if (optionalDepartment.isPresent()) {
      Department department = optionalDepartment.get();
      String hashedPassword = passwordEncoder.encode(userToRegister.getPassword());
      userToRegister.setPassword(hashedPassword);
      Long usernameSequence = userRepository.nextUsernameSequence();
      String username = userToRegister.getFirstName() + userToRegister.getSecondName() + "#" + usernameSequence;
      User user = UserToRegisterUserDTOConverter.convert(userToRegister);
      user.setDepartment(department);
      user.setUsername(username);
      user.setRole(UserRole.ROLE_ADMIN);
      userRepository.saveAndFlush(user);
      return new ResponseEntity<>(HttpStatus.CREATED);
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }
}