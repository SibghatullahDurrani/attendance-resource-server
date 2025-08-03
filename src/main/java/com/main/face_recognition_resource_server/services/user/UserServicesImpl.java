package com.main.face_recognition_resource_server.services.user;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeavesAllowedPolicyDTO;
import com.main.face_recognition_resource_server.DTOS.leave.RemainingLeavesDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ControlAcknowledgementDTO;
import com.main.face_recognition_resource_server.DTOS.user.*;
import com.main.face_recognition_resource_server.constants.*;
import com.main.face_recognition_resource_server.domains.*;
import com.main.face_recognition_resource_server.exceptions.*;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.repositories.user.UserRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import com.main.face_recognition_resource_server.services.shift.ShiftServices;
import com.main.face_recognition_resource_server.utilities.MessageMetadataWrapper;
import com.rabbitmq.client.Channel;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class UserServicesImpl implements UserServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationServices organizationServices;
    private final AttendanceRepository attendanceRepository;
    private final ShiftServices shiftServices;
    private final RabbitMQMessageBackupServices rabbitMQMessageBackupServices;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;


    public UserServicesImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganizationServices organizationServices, AttendanceRepository attendanceRepository, ShiftServices shiftServices, RabbitMQMessageBackupServices rabbitMQMessageBackupServices, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationServices = organizationServices;
        this.attendanceRepository = attendanceRepository;
        this.shiftServices = shiftServices;
        this.rabbitMQMessageBackupServices = rabbitMQMessageBackupServices;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
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

    @Override
    public Page<ShiftAllocationDTO> getUserShiftAllocations(Long organizationId, String fullName, Long departmentId, Long shiftId, PageRequest pageRequest) {
        AtomicBoolean isFilterApplied = new AtomicBoolean(false);
        Specification<User> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<User, Department> userDepartmentJoin = root.join("department", JoinType.INNER);
            Join<Department, Organization> userDepartmentOrganizationJoin = userDepartmentJoin.join("organization", JoinType.INNER);
            Join<User, Shift> userShiftJoin = root.join("userShift", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(userDepartmentOrganizationJoin.get("id"), organizationId));

            if (fullName != null && !fullName.isEmpty()) {
                String fullNameLower = fullName.toLowerCase();
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + fullNameLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("secondName")), "%" + fullNameLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(
                                criteriaBuilder.concat(
                                        root.get("fullName"),
                                        criteriaBuilder.concat(
                                                " ",
                                                root.get("secondName")
                                        )
                                )
                        ), "%" + fullNameLower + "%")
                ));
            }
            if (departmentId != null) {
                predicates.add(criteriaBuilder.equal(userDepartmentJoin.get("id"), departmentId));
                isFilterApplied.set(true);
            }
            if (shiftId != null) {
                predicates.add(criteriaBuilder.equal(userShiftJoin.get("id"), shiftId));
                isFilterApplied.set(true);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.getUserShiftAllocations(specification, pageRequest, isFilterApplied.get());
    }

    @Override
    @Transactional
    @Modifying
    public void changeUserShiftAllocations(List<EditedShiftAllocationDTO> editedShiftAllocations, Long organizationId) throws OrganizationDoesntBelongToYouException, UserDoesntExistException, JsonProcessingException, InvalidShiftSelectionException {
        for (EditedShiftAllocationDTO editedShiftAllocation : editedShiftAllocations) {
            checkIfOrganizationBelongsToUser(editedShiftAllocation.getUserId(), organizationId);

            User user = userRepository.findById(editedShiftAllocation.getUserId())
                    .orElseThrow(EntityNotFoundException::new);

            if (editedShiftAllocation.getNewShiftId() == null) {
                throw new InvalidShiftSelectionException("Invalid Shift");
            }
            if (editedShiftAllocation.getNewShiftMode() == null) {
                throw new InvalidShiftSelectionException("Invalid Shift Allocation Mode");
            }

            Shift shift = shiftServices.getShiftById(editedShiftAllocation.getNewShiftId());
            if (!shift.getOrganization().getId().equals(organizationId)) {
                throw new InvalidShiftSelectionException("Invalid Shift");
            }

            user.setUserShift(shift);
            user.getUserShiftSetting().setShiftMode(editedShiftAllocation.getNewShiftMode());
            if (editedShiftAllocation.getNewShiftMode() == ShiftMode.TEMPORARY) {
                if (editedShiftAllocation.getNewStartDate() == null || editedShiftAllocation.getNewEndDate() == null) {
                    throw new InvalidShiftSelectionException("Invalid Dates");
                }
                user.getUserShiftSetting().setStartDate(new Date(editedShiftAllocation.getNewStartDate()));
                user.getUserShiftSetting().setEndDate(new Date(editedShiftAllocation.getNewEndDate()));
            } else {
                user.getUserShiftSetting().setStartDate(null);
                user.getUserShiftSetting().setEndDate(null);
            }
            userRepository.save(user);
            String CONTROL_EXCHANGE_NAME = "control.exchange";
            String USER_CONTROL_ROUTING_KEY = "control." + organizationId + ".user.key";

            UserShiftChangeMessageDTO userShiftChangeMessageDTO = UserShiftChangeMessageDTO.builder()
                    .userId(user.getId())
                    .changedShiftId(user.getUserShift().getId())
                    .build();

            UserMessageDTO userMessage = UserMessageDTO.builder()
                    .userMessageType(UserMessageType.CHANGE_USER_SHIFT)
                    .payload(userShiftChangeMessageDTO)
                    .build();

            UUID backupMessageId = rabbitMQMessageBackupServices.backupMessageAndReturnId(userMessage, organizationId);
            String userMessageJson = objectMapper.writeValueAsString(userMessage);

            String messageMetaDataWrapper = objectMapper.writeValueAsString(
                    new MessageMetadataWrapper(backupMessageId, RabbitMQMessageType.USER)
            );
            CorrelationData correlationData = new CorrelationData(messageMetaDataWrapper);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uuid", backupMessageId.toString());
            messageProperties.setHeader("routingType", RabbitMQMessageType.USER.name());
            Message message = new Message(userMessageJson.getBytes(StandardCharsets.UTF_8), messageProperties);

            rabbitTemplate.convertAndSend(CONTROL_EXCHANGE_NAME, USER_CONTROL_ROUTING_KEY, message, correlationData);
        }
    }

    @Override
    @RabbitListener(queues = {"${user-control-acknowledgement-queue-name}"})
    public void handleUserAcknowledgementMessage(Message message, Channel channel) throws IOException {
        try {
            Logger logger = LoggerFactory.getLogger(UserServicesImpl.class);
            logger.info("Message received");
            byte[] messageBody = message.getBody();
            ControlAcknowledgementDTO controlAcknowledgementDTO = objectMapper.readValue(messageBody, ControlAcknowledgementDTO.class);
            logger.info("Received control acknowledgement {}", controlAcknowledgementDTO.toString());
            userRepository.findById(controlAcknowledgementDTO.getId()).ifPresent(user -> {
                user.setSavedInProducer(true);
                user.setLastSavedInProducerDate(new Date(controlAcknowledgementDTO.getSavedAt()));
                userRepository.saveAndFlush(user);
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            throw new RuntimeException(e);
        }

    }
}