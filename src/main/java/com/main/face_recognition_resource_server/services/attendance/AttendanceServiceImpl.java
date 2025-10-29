package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceLiveFeedDTO;
import com.main.face_recognition_resource_server.DTOS.user.UserLiveFeedMetaData;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.checkin.CheckInService;
import com.main.face_recognition_resource_server.services.checkout.CheckOutService;
import com.main.face_recognition_resource_server.services.department.DepartmentService;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import com.main.face_recognition_resource_server.services.shift.ShiftService;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserService userService;
    private final CheckInService checkInServices;
    private final CheckOutService checkOutService;
    private final OrganizationService organizationService;
    private final int scoreImageWidth = 1200;
    private final int scoreImageHeight = 100;
    private final Color green = new Color(67, 99, 63);
    private final Color red = new Color(163, 0, 0);
    private final Color cyan = new Color(209, 232, 111);
    private final SimpMessagingTemplate messagingTemplate;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final DepartmentService departmentService;
    private final ShiftService shiftService;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, UserService userService, CheckInService checkInServices, CheckOutService checkOutService, OrganizationService organizationService, SimpMessagingTemplate messagingTemplate, DepartmentService departmentService, ShiftService shiftService) {
        this.attendanceRepository = attendanceRepository;
        this.userService = userService;
        this.checkInServices = checkInServices;
        this.checkOutService = checkOutService;
        this.organizationService = organizationService;
        this.messagingTemplate = messagingTemplate;
        this.departmentService = departmentService;
        this.shiftService = shiftService;
    }

    @Override
    @Transactional
    @Async
    public void markCheckIn(Long userId, Long checkInTimestamp, BufferedImage fullImage, BufferedImage faceImage) throws UserDoesntExistException, IOException {
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfTimestampInTimeZone(checkInTimestamp, timeZone);
        Optional<Attendance> attendanceOptional = getUserAttendanceFromDayStartTillDate(userId, startAndEndDate);

        if (attendanceOptional.isPresent()) {
            Attendance attendance = attendanceOptional.get();
            if (attendance.getCheckIns() == null || attendance.getCheckIns().isEmpty()) {
                String checkInPolicyTime = userService.getUserCheckInTime(userId);
                Long organizationId = userService.getUserOrganizationIdByUserId(userId);

                int lateAttendanceToleranceTimePolicy = organizationService.getOrganizationLateAttendanceToleranceTimePolicy(organizationId);

                int lateAttendanceToleranceTimeHours = 0;
                int lateAttendanceToleranceTimeMinutes = 0;
                while (lateAttendanceToleranceTimePolicy >= 60) {
                    lateAttendanceToleranceTimeHours += 1;
                    lateAttendanceToleranceTimePolicy -= 60;
                }
                lateAttendanceToleranceTimeMinutes += lateAttendanceToleranceTimePolicy;


                String[] timeSplit = checkInPolicyTime.split(":");
                int hour = Integer.parseInt(timeSplit[0]);
                int minute = Integer.parseInt(timeSplit[1]);

                LocalTime baseTime = LocalTime.of(hour, minute);
                LocalTime adjustedTime = baseTime
                        .plusHours(lateAttendanceToleranceTimeHours)
                        .plusMinutes(lateAttendanceToleranceTimeMinutes);

                String userTimeZone = userService.getUserTimeZone(userId);
                ZoneId zone = ZoneId.of(userTimeZone);

                LocalDate todayInTimeZone = LocalDate.now(zone);
                LocalDateTime localDateTime = todayInTimeZone.atTime(adjustedTime);

                Instant rquiredTimeInstant = localDateTime.atZone(zone).toInstant();
                long requiredCheckInTimestamp = rquiredTimeInstant.toEpochMilli();

                AttendanceStatus attendanceStatus;
                if (checkInTimestamp > requiredCheckInTimestamp) {
                    attendanceStatus = AttendanceStatus.LATE;
                } else {
                    attendanceStatus = AttendanceStatus.ON_TIME;
                }

                attendance.setStatus(attendanceStatus);
                attendance.setCurrentAttendanceStatus(AttendanceType.CHECK_IN);
                attendanceRepository.saveAndFlush(attendance);

                Instant checkInInstant = Instant.ofEpochMilli(checkInTimestamp);
                checkInServices.saveCheckIn(checkInInstant, attendance, fullImage, faceImage);

                String sourceImageURI = "SourceFaces/%s%s".formatted(userId, ".jpg");
                Path sourceImagePath = Paths.get(sourceImageURI);
                byte[] sourceImage = Files.readAllBytes(sourceImagePath);
                UserLiveFeedMetaData userLiveFeedMetaData = userService.getUserLiveFeedMetaData(userId);
                sendLiveAttendanceFeed(
                        userService.getUserOrganizationIdByUserId(userId),
                        AttendanceLiveFeedDTO.builder()
                                .userId(userId)
                                .fullName(userLiveFeedMetaData.getFullName())
                                .designation(userLiveFeedMetaData.getDesignation())
                                .departmentName(userLiveFeedMetaData.getDepartmentName())
                                .attendanceType(AttendanceType.CHECK_IN)
                                .attendanceStatus(attendanceStatus)
                                .checkInTime(checkInTimestamp)
                                .checkOutTime(0L)
                                .sourceImage(sourceImage)
                                .build());

            } else {
                attendance.setCurrentAttendanceStatus(AttendanceType.CHECK_IN);
                attendanceRepository.saveAndFlush(attendance);
                Instant checkInInstant = Instant.ofEpochMilli(checkInTimestamp);
                checkInServices.saveCheckIn(checkInInstant, attendance, fullImage, faceImage);
            }
        }
    }

    @Override
    @Transactional
    @Async
    public void markCheckOut(Long userId, Long checkOutTimestamp, BufferedImage fullImage, BufferedImage faceImage) throws IOException, UserDoesntExistException {
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfTimestampInTimeZone(checkOutTimestamp, timeZone);
        Optional<Attendance> attendance = getUserAttendanceFromDayStartTillDate(userId, startAndEndDate);

        if (attendance.isPresent()) {
            Instant checkOutInstant = Instant.ofEpochMilli(checkOutTimestamp);
            checkOutService.saveCheckOut(checkOutInstant, attendance.get(), fullImage, faceImage);
            attendance.get().setCurrentAttendanceStatus(AttendanceType.CHECK_OUT);
            attendanceRepository.saveAndFlush(attendance.get());
            UserLiveFeedMetaData userLiveFeedMetaData = userService.getUserLiveFeedMetaData(userId);
            String sourceImageURI = "SourceFaces/%s%s".formatted(userId, ".jpg");
            Path sourceImagePath = Paths.get(sourceImageURI);
            byte[] sourceImage = Files.readAllBytes(sourceImagePath);
            Instant firstCheckIn = checkInServices.getFirstCheckInOfAttendanceId(attendance.get().getId());
            sendLiveAttendanceFeed(
                    userService.getUserOrganizationIdByUserId(userId),
                    AttendanceLiveFeedDTO.builder()
                            .userId(userId)
                            .fullName(userLiveFeedMetaData.getFullName())
                            .designation(userLiveFeedMetaData.getDesignation())
                            .departmentName(userLiveFeedMetaData.getDepartmentName())
                            .attendanceType(AttendanceType.CHECK_OUT)
                            .attendanceStatus(attendance.get().getStatus())
                            .checkOutTime(checkOutTimestamp)
                            .checkInTime(firstCheckIn.toEpochMilli())
                            .sourceImage(sourceImage)
                            .build());
        }
    }

    @Override
    public void markAbsentOfUsersOfOrganizationForDate(Long organizationId, Instant markAbsentTime, DayOfWeek dayOfWeek) {
        boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(organizationId, markAbsentTime);

        if (!exists) {
            List<Long> userIds = userService.getAllUserIdsOfOrganization(organizationId);
            List<Long> userIdsOfLeave = attendanceRepository.getUserIdsOfLeaveOfDate(markAbsentTime);
            List<Attendance> attendances = new ArrayList<>();
            for (Long userId : userIds) {
                if (!userIdsOfLeave.contains(userId)) {
                    boolean isWorkingDay = shiftService.isUserWorkingDay(dayOfWeek, userId);
                    if (isWorkingDay) {
                        attendances.add(Attendance.builder().user(User.builder().id(userId).build()).date(markAbsentTime).status(AttendanceStatus.ABSENT).build());
                    } else {
                        attendances.add(Attendance.builder().user(User.builder().id(userId).build()).date(markAbsentTime).status(AttendanceStatus.OFF_DAY).build());
                    }
                }
            }
            attendanceRepository.saveAllAndFlush(attendances);
        }
    }


    private int drawRectangle(Graphics scoreGraphics, int previousPoint, long startTime, long endTime, long totalTime, Color color) {
        double colorPercentage = (double) (endTime - startTime) / totalTime;
        int width = (int) (scoreImageWidth * colorPercentage);
        scoreGraphics.setColor(color);
        scoreGraphics.fillRect(previousPoint, 0, width, scoreImageHeight);
        return width;
    }

    private void drawRectangleTillEnd(Graphics scoreGraphics, int previousPoint, Color color) {
        scoreGraphics.setColor(color);
        scoreGraphics.fillRect(previousPoint, 0, scoreImageWidth - previousPoint, scoreImageHeight);
    }

    @Override
    public void sendLiveAttendanceFeed(Long organizationId, AttendanceLiveFeedDTO attendanceLiveFeedDTO) {
        String destination = "/topic/attendance-feed/" + organizationId;
        messagingTemplate.convertAndSend(destination, attendanceLiveFeedDTO);
    }

    @Override
    public List<AttendanceLiveFeedDTO> getRecentAttendancesOfOrganization(long organizationId) {
        List<Long> userIds = userService.getAllUserIdsOfOrganization(organizationId);
        String timeZone = organizationService.getOrganizationTimeZone(organizationId);
        Instant date = DateUtils.getStartDateOfToday(timeZone);
        List<Long> attendanceIds = attendanceRepository.getAllAttendanceIdsOfPresentUsersOfDate(userIds, date);

        return checkInServices.getFirstCheckInsOfAttendanceIdsForLiveAttendanceFeed(attendanceIds);
    }


    @Override
    public void markLeaveOfUserOnDate(Long userId, Instant date) {
        Attendance attendance = Attendance.builder().user(User.builder().id(userId).build()).date(date).status(AttendanceStatus.ON_LEAVE).build();
        attendanceRepository.saveAndFlush(attendance);
    }

    private Optional<Attendance> getUserAttendanceFromDayStartTillDate(Long userId, Instant[] startAndEndDate) {
        return attendanceRepository.getAttendanceByUserIdAndDate(userId, startAndEndDate[0], startAndEndDate[1]);
    }
}
