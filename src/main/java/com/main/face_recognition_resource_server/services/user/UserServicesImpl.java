package com.main.face_recognition_resource_server.services.user;


import com.main.face_recognition_resource_server.DTOS.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServicesImpl implements UserServices {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServicesImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDTO getUserDataByUsername(String username) throws UserDoesntExistException {
    Optional<UserDTO> optionalUser = userRepository.getUserByUsername(username);
    if (optionalUser.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return optionalUser.get();
    }
  }

  @Override
  public Page<UserDTO> getAllUsers(Pageable pageable) {
    return userRepository.getAllUsers(pageable);
  }

  @Override
  @Transactional
  public void registerUser(RegisterUserDTO userToRegister) {
    boolean userExistsWithEmailAndRole = userExistsWithEmailAndRole(userToRegister.getEmail(), userToRegister.getRole());
    if (!userExistsWithEmailAndRole) {
      String hashedPassword = passwordEncoder.encode(userToRegister.getPassword());
      Long usernameSequence = userRepository.nextUsernameSequence();
      String username = userToRegister.getFirstName() + userToRegister.getSecondName() + "#" + usernameSequence;
      userRepository.registerUser(
              userToRegister.getFirstName(),
              userToRegister.getSecondName(),
              hashedPassword,
              username,
              userToRegister.getRole().toString(),
              userToRegister.getIdentificationNumber(),
              userToRegister.getEmail(),
              userToRegister.getDepartmentId()
      );
    }
  }

  @Override
  public boolean userExistsWithEmailAndRole(String email, UserRole role) throws UserAlreadyExistsException {
    boolean exists = userRepository.existsByEmailAndRole(email, role);
    if (exists) {
      throw new UserAlreadyExistsException();
    } else {
      return false;
    }
  }

  @Override
  public Long getUserOrganizationId(String username) throws UserDoesntExistException {
    Optional<Long> userOrganizationId = userRepository.getUserOrganizationId(username);
    if (userOrganizationId.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return userOrganizationId.get();
    }
  }

}