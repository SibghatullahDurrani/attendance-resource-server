package com.main.face_recognition_resource_server.repositories.user;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.user.*;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>, UserCriteriaRepository {
    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.user.UserDTO(
            u.id, u.firstName, u.secondName, u.username, u.department.departmentName,
            u.department.organization.id,u.role, u.profilePictureName, u.sourceFacePictureName
            ) FROM User u WHERE u.username = ?1
            """)
    Optional<UserDTO> getUserDTOByUsername(String username);

    @Query("""
            SELECT u FROM User u WHERE u.username = ?1
            """)
    Optional<User> getUserByUsername(String username);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.user.UserDTO(
            u.id, u.firstName, u.secondName, u.username, u.department.departmentName,
            u.department.organization.id,u.role, u.profilePictureName, u.sourceFacePictureName
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

    @Query("""
            SELECT u.department.organization.id FROM User u WHERE u.username = ?1
            """)
    Optional<Long> getUserOrganizationId(String username);

    @Query("""
            SELECT u.department.organization.id FROM User u WHERE u.id = ?1
            """)
    Optional<Long> getUserOrganizationId(Long userId);

    boolean existsByEmailAndRole(String email, UserRole role);

    @Query(value = "INSERT INTO users (first_name, second_name,password,username,role,identification_number,email,department_id,remaining_sick_leaves,remaining_annual_leaves,designation,phone_number,source_face_picture_name)" + "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10)", nativeQuery = true)
    @Transactional
    @Modifying
    void registerUser(String firstName, String secondName, String hashedPassword, String username, String role, String identificationNumber, String email, Long departmentId, int sickLeavesAllowed, int annualLeavesAllowed);

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

    @Query("""
            SELECT CONCAT(u.firstName, ' ',u.secondName) FROM User u WHERE  u.id = ?1
            """)
    String getUserFullNameByUserId(Long userId);

    @Query("""
            SELECT u.id FROM User u WHERE u.department.organization.id = ?1
            """)
    List<Long> getAllUserIdsOfOrganization(long organizationId);

    @Query("""
            SELECT COUNT(*) FILTER(WHERE u.department.id = ?1) FROM User u
            """)
    Long getTotalUsersOfDepartment(Long departmentId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.user.AdminUsersTableRecordDTO(
                      u.id, u.firstName, u.secondName, u.department.departmentName, u.designation,
                      u.identificationNumber, u.email, u.phoneNumber
            )FROM User u WHERE u.department.organization.id = ?1
            """)
    Page<AdminUsersTableRecordDTO> getUsersPageOfOrganization(Long organizationId, Pageable pageRequest);

    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(username FROM '#([0-9]+)$')AS INTEGER ) ),0) FROM users
            WHERE username LIKE CONCAT(:base, '#%')
            """, nativeQuery = true)
    int getUsernameSuffixByBase(@Param("base") String base);

    boolean existsByUsername(String username);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE users SET source_face_picture_name = ?1
            WHERE id = ?2
            """, nativeQuery = true)
    void setUserSourceImage(String sourceImageName, Long id);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.user.UserDataDTO(
                      u.id,
                      u.firstName, u.secondName, u.username, u.identificationNumber,
                      u.department.departmentName, u.designation, u.sourceFacePictureName,
                      u.phoneNumber
            ) FROM User u WHERE u.id = ?1
            """)
    UserDataDTO getUserData(Long userId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.user.SearchUserDTO(
            u.id, CONCAT(u.firstName, ' ', u.secondName), u.department.departmentName
            ) FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', ?1, '%'))
            OR LOWER(u.secondName) LIKE LOWER(CONCAT('%', ?1, '%')) AND u.department.organization.id = ?2
            """)
    List<SearchUserDTO> searchUserByNameOfOrganization(String name, Long organizationId);

    @Query("""
            SELECT u.id FROM User u WHERE u.department.id IN ?1
            """)
    List<Long> getAllUserIdsOfDepartments(List<Long> departmentIds);

    @Query("""
            SELECT u.id FROM User u
            """)
    List<Long> getAllUserIds();

    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            WHERE LOWER(u.firstName) = ?1 AND LOWER(u.secondName) = ?2 AND u.identificationNumber = ?3
            """)
    boolean existsByNameAndIdentificationNumber(String firstName, String secondName, String identificationNumber);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.user.UserLiveFeedMetaData(
                      CONCAT(u.firstName,CONCAT(' ', u.secondName)), u.designation, u.department.departmentName
            ) FROM User u WHERE u.id = ?1
            """)
    UserLiveFeedMetaData getUserLiveFeedMetaData(Long userId);
}
