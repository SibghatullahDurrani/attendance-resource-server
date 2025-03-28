package com.main.face_recognition_resource_server.services.user;


import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeavesAllowedPolicyDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.user.RegisterUserDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServicesImpl implements UserServices {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final OrganizationServices organizationServices;

  public UserServicesImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganizationServices organizationServices) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.organizationServices = organizationServices;
  }

  @Override
  public UserDTO getUserDataByUsername(String username) throws UserDoesntExistException {
    Optional<UserDTO> optionalUser = userRepository.getUserDTOByUsername(username);
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
  public void registerUser(RegisterUserDTO userToRegister, Long organizationId) throws UserAlreadyExistsException {
    boolean userExistsWithEmailAndRole = userExistsWithEmailAndRole(userToRegister.getEmail(), userToRegister.getRole());
    if (!userExistsWithEmailAndRole) {
      String hashedPassword = passwordEncoder.encode(userToRegister.getPassword());
      Long usernameSequence = userRepository.nextUsernameSequence();
      String username = userToRegister.getFirstName() + userToRegister.getSecondName() + "#" + usernameSequence;
      LeavesAllowedPolicyDTO organizationLeavesPolicy = organizationServices.getOrganizationLeavesPolicy(organizationId);
      userRepository.registerUser(
              userToRegister.getFirstName(),
              userToRegister.getSecondName(),
              hashedPassword,
              username,
              userToRegister.getRole().toString(),
              userToRegister.getIdentificationNumber(),
              userToRegister.getEmail(),
              userToRegister.getDepartmentId(),
              organizationLeavesPolicy.getSickLeavesAllowed(),
              organizationLeavesPolicy.getAnnualLeavesAllowed()
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

  @Override
  public DepartmentDTO getDepartmentByUsername(String username) throws UserDoesntExistException {
    Optional<DepartmentDTO> department = userRepository.getDepartmentByUsername(username);
    if (department.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return department.get();
    }
  }

  @Override
  public OrganizationDTO getOrganizationByUsername(String username) throws UserDoesntExistException {
    Optional<OrganizationDTO> organizationByUsername = userRepository.getOrganizationByUsername(username);
    if (organizationByUsername.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return organizationByUsername.get();
    }
  }

  @Override
  public Long getUserDepartmentId(String username) throws UserDoesntExistException {
    Optional<Long> departmentId = userRepository.getUserDepartmentId(username);
    if (departmentId.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return departmentId.get();
    }
  }

  @Override
  public User getUserById(Long userId) throws UserDoesntExistException {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return user.get();
    }
  }

  @Override
  public boolean userExistsWithUserId(Long userId) {
    return userRepository.existsById(userId);
  }

  @Override
  public Long getUserOrganizationIdByUserId(Long userId) throws UserDoesntExistException {
    Optional<Long> organizationId = this.userRepository.getUserOrganizationIdByUserId(userId);
    if (organizationId.isPresent()) {
      return organizationId.get();
    } else {
      throw new UserDoesntExistException();
    }
  }

  @Override
  public List<User> getUsersByOrganizationId(Long organizationId) {
    return userRepository.getUsersByOrganizationId(organizationId);
  }

  @Override
  public Long getUserIdByUsername(String username) throws UserDoesntExistException {
    Optional<Long> userId = userRepository.getUserIdByUsername(username);
    if (userId.isPresent()) {
      return userId.get();
    } else {
      throw new UserDoesntExistException();
    }
  }

  @Override
  public User getUserByUsername(String username) throws UserDoesntExistException {
    Optional<User> user = userRepository.getUserByUsername(username);
    if (user.isEmpty()) {
      throw new UserDoesntExistException();
    } else {
      return user.get();
    }
  }

  @Override
  public User saveUser(User user) {
    return userRepository.saveAndFlush(user);
  }

  @Override
  public RemainingLeavesDTO getRemainingLeavesOfUser(String username) {
    return userRepository.getRemainingLeavesByUsername(username);
  }

}