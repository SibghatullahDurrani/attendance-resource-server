package com.main.face_recognition_resource_server.services.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.user.*;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.*;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface UserService {
    UserDTO getUserDataByUsername(String username) throws UserDoesntExistException;

    Page<UserDTO> getAllUsers(Pageable pageable);

    void registerUser(RegisterUserDTO userToRegister, Long organizationId) throws UserAlreadyExistsException, SQLException, IOException, UserAlreadyExistsWithIdentificationNumberException;

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

    String getUserFullNameByUserId(Long userId);

    List<Long> getAllUserIdsOfOrganization(long organizationId);

    void checkIfOrganizationBelongsToUser(Long organizationId, String username) throws UserDoesntExistException, OrganizationDoesntBelongToYouException;

    void checkIfOrganizationBelongsToUser(Long userId, Long organizationId) throws UserDoesntExistException, OrganizationDoesntBelongToYouException;

    Long getTotalUsersOfDepartment(Long departmentId);

    Page<AdminUsersTableRecordDTO> getUsersPageOfOrganization(Long organizationId, Pageable pageRequest);

    Page<AdminUsersTableRecordDTO> getUsersPageOfOrganization(Long organizationId, Pageable pageRequest, String fullName, Long departmentId, String designation, Boolean isSourceFaceRegistered, String identificationNumber);

    UserDataDTO getUserData(Long userId);

    List<SearchUserDTO> searchUserByNameOfOrganization(String name, Long organizationId);

    List<Long> getAllUserIdsOfDepartments(List<Long> departmentIds);

    List<Long> getAllUserIds();

    UserLiveFeedMetaData getUserLiveFeedMetaData(Long userId);

    Page<ShiftAllocationDTO> getUserShiftAllocations(Long organizationId, String fullName, Long departmentId, Long shiftId, PageRequest pageRequest);

    void changeUserShiftAllocations(List<EditedShiftAllocationDTO> editedShiftAllocations, Long organizationId) throws OrganizationDoesntBelongToYouException, UserDoesntExistException, JsonProcessingException, InvalidShiftSelectionException;

    void handleUserAcknowledgementMessage(Message message, Channel channel) throws IOException;

    void registerUserViaCSV(String content, Long organizationId) throws IOException, UserAlreadyExistsWithIdentificationNumberException, SQLException, UserAlreadyExistsException;

    String getUserCheckInTime(Long userId);
}