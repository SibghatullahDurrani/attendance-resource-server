package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.User;
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
          SELECT new com.main.face_recognition_resource_server.DTOS.UserDTO(
            u.id, u.firstName, u.secondName, u.username, u.role, u.identificationNumber,
            u.email, u.department.departmentName, u.department.organization.organizationName
          ) FROM User u WHERE u.username = ?1
          """)
  Optional<UserDTO> getUserByUsername(String username);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.UserDTO(
          u.id, u.firstName, u.secondName, u.username, u.role, u.identificationNumber,
          u.email, u.department.departmentName, u.department.organization.organizationName
          ) FROM User u
          """)
  List<UserDTO> getAllUsers();

  @Query(value = "SELECT nextval('username_sequence')", nativeQuery = true)
  Long nextUsernameSequence();

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.OrganizationDTO(
          u.department.organization.id, u.department.organization.organizationName,
          u.department.organization.organizationType
          ) FROM User u WHERE u.username = ?1
          """)
  OrganizationDTO getOrganizationByUsername(String username);

  @Query(
          """
                  SELECT u.department.organization.id FROM User u WHERE u.username = ?1
                  """
  )
  Long getUserOrganizationId(String username);

  boolean existsByEmailAndRole(String email, UserRole role);

  @Query(value = "INSERT INTO users (first_name, second_name,password,username,role,identification_number,email,department_id)" +
          "VALUES (?1,?2,?3,?4,?5,?6,?7,?8)", nativeQuery = true)
  @Transactional
  @Modifying
  void registerUser(String firstName,
                    String secondName,
                    String hashedPassword,
                    String username,
                    String role,
                    String identificationNumber,
                    String email,
                    Long departmentId);
}
