package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.UserDTO;
import com.main.face_recognition_resource_server.domains.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
  Optional<UserDTO> getOwnDetails(String username);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.UserDTO(
          u.id, u.firstName, u.secondName, u.username, u.role, u.identificationNumber,
          u.email, u.department.departmentName, u.department.organization.organizationName
          ) FROM User u
          """)
  List<UserDTO> getAllUserDetails();
}
