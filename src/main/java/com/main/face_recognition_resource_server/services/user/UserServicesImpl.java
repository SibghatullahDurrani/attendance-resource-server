package com.main.face_recognition_resource_server.services.user;


import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeavesAllowedPolicyDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.user.*;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.constants.UsernameType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntBelongToYouException;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.UserAlreadyExistsWithIdentificationNumberException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Service
public class UserServicesImpl implements UserServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationServices organizationServices;
    private final AttendanceRepository attendanceRepository;


    public UserServicesImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganizationServices organizationServices, AttendanceRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationServices = organizationServices;
        this.attendanceRepository = attendanceRepository;
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
    @Retryable(retryFor = SQLException.class)
    public void registerUser(RegisterUserDTO userToRegister, Long organizationId) throws UserAlreadyExistsException, SQLException, IOException, UserAlreadyExistsWithIdentificationNumberException {
//    boolean userExistsWithEmailAndRole = userExistsWithEmailAndRole(userToRegister.getEmail(), userToRegister.getRole());
        boolean userExistsWithIdentificationNumber = userExistsWithIdentificationNumber(userToRegister.getFirstName(), userToRegister.getSecondName(), userToRegister.getIdentificationNumber());
        if (!userExistsWithIdentificationNumber) {
            String hashedPassword = passwordEncoder.encode(userToRegister.getPassword());
            String username;
            if (userToRegister.getUsernameType() == UsernameType.USERNAME_AS_PHONE_NUMBER) {
                username = userToRegister.getPhoneNumber();
                if (userRepository.existsByUsername(username)) {
                    throw new UserAlreadyExistsException();
                }
            } else if (userToRegister.getUsernameType() == UsernameType.USERNAME_AS_IDENTIFICATION_NUMBER) {
                username = userToRegister.getIdentificationNumber();
                if (userRepository.existsByUsername(username)) {
                    throw new UserAlreadyExistsException();
                }
            } else {
                String cleanedBase = (userToRegister.getFirstName() + userToRegister.getSecondName())
                        .replaceAll("//s", "")
                        .replaceAll("#", "");
                username = generateUsernameByFirstAndSecondName(cleanedBase);
            }
            LeavesAllowedPolicyDTO organizationLeavesPolicy = organizationServices.getOrganizationLeavesPolicy(organizationId);
            User user = User.builder()
                    .firstName(userToRegister.getFirstName())
                    .secondName(userToRegister.getSecondName())
                    .password(hashedPassword)
                    .username(username)
                    .role(userToRegister.getRole())
                    .identificationNumber(userToRegister.getIdentificationNumber())
                    .phoneNumber(userToRegister.getPhoneNumber())
                    .email(userToRegister.getEmail())
                    .designation(userToRegister.getDesignation())
                    .department(Department.builder()
                            .id(userToRegister.getDepartmentId()).build())
                    .remainingSickLeaves(organizationLeavesPolicy.getSickLeavesAllowed())
                    .remainingAnnualLeaves(organizationLeavesPolicy.getAnnualLeavesAllowed())
                    .build();

            user = userRepository.saveAndFlush(user);

            byte[] imageBytes = Base64.getDecoder().decode(userToRegister.getSourceImageBase64());
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (bufferedImage == null) {
                throw new IOException("Failed to decode image");
            }
            File registerDir = new File("RegisterFaces");
            if (!registerDir.exists()) {
                registerDir.mkdirs();
            }
            File sourceFacesDir = new File("SourceFaces");
            if (!sourceFacesDir.exists()) {
                sourceFacesDir.mkdirs();
            }

            File registerOutputFile = new File(registerDir, user.getId() + ".jpg");
            File sourceFacesOutputFile = new File(sourceFacesDir, user.getId() + ".jpg");

            ImageIO.write(bufferedImage, "jpg", registerOutputFile);
            ImageIO.write(bufferedImage, "jpg", sourceFacesOutputFile);
            userRepository.setUserSourceImage(user.getId() + ".jpg", user.getId());

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Attendance attendance = Attendance.builder()
                    .user(user)
                    .date(calendar.getTime())
                    .status(AttendanceStatus.ABSENT)
                    .build();

            attendanceRepository.saveAndFlush(attendance);
        }
    }

    private boolean userExistsWithIdentificationNumber(String firstName, String secondName, String identificationNumber) throws UserAlreadyExistsWithIdentificationNumberException {
        boolean exists = userRepository.existsByNameAndIdentificationNumber(firstName.toLowerCase(), secondName.toLowerCase(), identificationNumber);
        if (exists) {
            throw new UserAlreadyExistsWithIdentificationNumberException("Member: " + firstName + " " + secondName + " already exists with CNIC#: " + identificationNumber);
        } else {
            return false;
        }
    }

    @Transactional
    @Retryable(retryFor = SQLException.class)
    protected String generateUsernameByFirstAndSecondName(String base) throws SQLException {
        int suffix = userRepository.getUsernameSuffixByBase(base) + 1;
        String username = base + "#" + suffix;
        boolean usernameExists = userRepository.existsByUsername(username);
        if (usernameExists) {
            throw new SQLException("username exists");
        } else {
            return username;
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

    @Override
    public String getUserFullNameByUserId(Long userId) {
        return userRepository.getUserFullNameByUserId(userId);
    }

    @Override
    public List<Long> getAllUserIdsOfOrganization(long organizationId) {
        return userRepository.getAllUserIdsOfOrganization(organizationId);
    }

    @Override
    public void checkIfOrganizationBelongsToUser(Long organizationId, String username) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Optional<Long> userOrganizationId = userRepository.getUserOrganizationId(username);
        if (userOrganizationId.isEmpty()) {
            throw new UserDoesntExistException();
        } else {
            if (!userOrganizationId.get().equals(organizationId)) {
                throw new OrganizationDoesntBelongToYouException();
            }
        }
    }

    @Override
    public void checkIfOrganizationBelongsToUser(Long userId, Long organizationId) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Optional<Long> userOrganizationId = userRepository.getUserOrganizationId(userId);
        if (userOrganizationId.isEmpty()) {
            throw new UserDoesntExistException();
        } else {
            if (!userOrganizationId.get().equals(organizationId)) {
                throw new OrganizationDoesntBelongToYouException();
            }
        }
    }

    @Override
    public Long getTotalUsersOfDepartment(Long departmentId) {
        return userRepository.getTotalUsersOfDepartment(departmentId);
    }

    @Override
    public Page<AdminUsersTableRecordDTO> getUsersPageOfOrganization(Long organizationId, Pageable pageRequest) {
        return userRepository.getUsersPageOfOrganization(organizationId, pageRequest);
    }

    @Override
    public UserDataDTO getUserData(Long userId) {
        return userRepository.getUserData(userId);
    }

    @Override
    public List<SearchUserDTO> searchUserByNameOfOrganization(String name, Long organizationId) {
        return userRepository.searchUserByNameOfOrganization(name, organizationId);
    }

    @Override
    public List<Long> getAllUserIdsOfDepartments(List<Long> departmentIds) {
        return userRepository.getAllUserIdsOfDepartments(departmentIds);
    }

    @Override
    public List<Long> getAllUserIds() {
        return userRepository.getAllUserIds();
    }

    @Override
    public UserLiveFeedMetaData getUserLiveFeedMetaData(Long userId) {
        return userRepository.getUserLiveFeedMetaData(userId);
    }
}