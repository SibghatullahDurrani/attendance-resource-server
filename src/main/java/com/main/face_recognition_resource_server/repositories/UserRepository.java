package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.user.UserDTO(
          u.id, u.firstName, u.secondName, u.username, u.department.departmentName, u.role,
          u.profilePicturePath
          ) FROM User u WHERE u.username = ?1
          """)
  Optional<UserDTO> getUserDTOByUsername(String username);

  @Query("""
          SELECT u FROM User u WHERE u.username = ?1
          """)
  Optional<User> getUserByUsername(String username);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.user.UserDTO(
          u.id, u.firstName, u.secondName, u.username, u.department.departmentName, u.role,
          u.profilePicturePath
          ) FROM User u
          """)
  Page<UserDTO> getAllUsers(Pageable pageable);

  @Query(value = "SELECT nextval('username_sequence')", nativeQuery = true)
  Long nextUsernameSequence();

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO(
          u.department.organization.id, u.department.organization.organizationName,
          u.department.organization.organizationType
          ) FROM User u WHERE u.username = ?1
          """)
  Optional<OrganizationDTO> getOrganizationByUsername(String username);

  @Query(
          """
                  SELECT u.department.organization.id FROM User u WHERE u.username = ?1
                  """
  )
  Optional<Long> getUserOrganizationId(String username);

  boolean existsByEmailAndRole(String email, UserRole role);

  @Query(value = "INSERT INTO users (first_name, second_name,password,username,role,identification_number,email,department_id,remaining_sick_leaves,remaining_annual_leaves)" +
          "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10)", nativeQuery = true)
  @Transactional
  @Modifying
  void registerUser(String firstName,
                    String secondName,
                    String hashedPassword,
                    String username,
                    String role,
                    String identificationNumber,
                    String email,
                    Long departmentId,
                    int sickLeavesAllowed,
                    int annualLeavesAllowed
  );

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO(
                    u.department.id, u.department.departmentName
          ) FROM User u WHERE u.username = ?1
          """)
  Optional<DepartmentDTO> getDepartmentByUsername(String username);

  @Query("""
          SELECT u.department.id FROM User u WHERE u.username = ?1
          """)
  Optional<Long> getUserDepartmentId(String username);

  @Query("""
          SELECT u.department.organization.id FROM User u WHERE u.id = ?1
          """)
  Optional<Long> getUserOrganizationIdByUserId(Long userId);

  @Query("""
          SELECT u FROM User u WHERE u.department.organization.id = ?1
          """)
  List<User> getUsersByOrganizationId(Long organizationId);

  @Query("""
          SELECT u.id FROM User u WHERE u.username = ?1
          """)
  Optional<Long> getUserIdByUsername(String username);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO(
                    u.remainingSickLeaves, u.remainingAnnualLeaves
          ) FROM User u WHERE u.username = ?1
          """)
  RemainingLeavesDTO getRemainingLeavesByUsername(String username);
}
