package com.main.face_recognition_resource_server.services.attendance.reports;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyUserAttendanceDTO;
import com.main.face_recognition_resource_server.TestDataHelper;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.domains.*;
import com.main.face_recognition_resource_server.repositories.*;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.repositories.shift.ShiftRepository;
import com.main.face_recognition_resource_server.repositories.user.UserRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AttendanceReportsServiceTest {

    @Autowired
    private AttendanceReportsService attendanceReportsService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private CheckOutRepository checkOutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private UserShiftRepository userShiftRepository;

    @Autowired
    private OrganizationService organizationService;

    private Organization organization;
    private Department department1;
    private Department department2;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Clean up
        checkInRepository.deleteAll();
        checkOutRepository.deleteAll();
        attendanceRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();

        // Create test data
        OrganizationPolicies policies = TestDataHelper.createOrganizationPolicies();
        organization = organizationRepository.save(TestDataHelper.createOrganization(policies));
        
        department1 = Department.builder()
                .departmentName("Engineering")
                .organization(organization)
                .build();
        department1 = departmentRepository.save(department1);
        
        department2 = Department.builder()
                .departmentName("Sales")
                .organization(organization)
                .build();
        department2 = departmentRepository.save(department2);

        Shift shift = shiftRepository.save(TestDataHelper.createShift(organization));

        UserShiftSetting setting1 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting2 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting3 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());

        // Create users with custom names
        User baseUser1 = TestDataHelper.createUser(department1, shift, setting1, "john.doe");
        user1 = userRepository.save(baseUser1);

        User baseUser2 = TestDataHelper.createUser(department1, shift, setting2, "jane.smith");  
        user2 = userRepository.save(baseUser2);

        User baseUser3 = TestDataHelper.createUser(department2, shift, setting3, "bob.wilson");
        user3 = userRepository.save(baseUser3);
    }

    @Test
    void testGetDailyUserAttendancesForToday() throws IOException {
        // Given - Create attendance records for today
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant todayInstant = todayStart.plusHours(9).toInstant(); // 9 AM

        // Create attendance records
        Attendance attendance1 = Attendance.builder()
                .user(user1)
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build();
        attendance1 = attendanceRepository.save(attendance1);

        Attendance attendance2 = Attendance.builder()
                .user(user2)
                .date(todayInstant.plus(30, ChronoUnit.MINUTES))
                .status(AttendanceStatus.LATE)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build();
        attendance2 = attendanceRepository.save(attendance2);

        Attendance attendance3 = Attendance.builder()
                .user(user3)
                .date(todayInstant.plus(15, ChronoUnit.MINUTES))
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_OUT)
                .build();
        attendance3 = attendanceRepository.save(attendance3);

        // Create CheckIn records
        checkInRepository.save(TestDataHelper.createCheckIn(attendance1, todayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance2, todayInstant.plus(30, ChronoUnit.MINUTES)));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance3, todayInstant.plus(15, ChronoUnit.MINUTES)));

        // Create CheckOut records
        checkOutRepository.save(TestDataHelper.createCheckOut(attendance1, todayInstant.plus(8, ChronoUnit.HOURS)));
        checkOutRepository.save(TestDataHelper.createCheckOut(attendance3, todayInstant.plus(9, ChronoUnit.HOURS)));

        // When - Query without filters
        Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), null, null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testGetDailyUserAttendancesWithAttendanceTypeFilter() throws IOException {
        // Given
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant todayInstant = todayStart.plusHours(9).toInstant();

        Attendance attendance1 = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance attendance2 = attendanceRepository.save(Attendance.builder()
                .user(user2)
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_OUT)
                .build());

        checkInRepository.save(TestDataHelper.createCheckIn(attendance1, todayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance2, todayInstant));

        // When - Filter by CHECK_IN type
        Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), AttendanceType.CHECK_IN, null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(user1.getId());
    }

    @Test
    void testGetDailyUserAttendancesWithAttendanceStatusFilter() throws IOException {
        // Given
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant todayInstant = todayStart.plusHours(9).toInstant();

        Attendance attendance1 = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance attendance2 = attendanceRepository.save(Attendance.builder()
                .user(user2)
                .date(todayInstant)
                .status(AttendanceStatus.LATE)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance attendance3 = attendanceRepository.save(Attendance.builder()
                .user(user3)
                .date(todayInstant)
                .status(AttendanceStatus.ABSENT)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        checkInRepository.save(TestDataHelper.createCheckIn(attendance1, todayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance2, todayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance3, todayInstant));

        // When - Filter by PRESENT (includes ON_TIME and LATE)
        Page<DailyUserAttendanceDTO> presentResult = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), null, AttendanceStatusFilter.PRESENT, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(presentResult.getTotalElements()).isEqualTo(2);

        // When - Filter by ON_TIME only
        Page<DailyUserAttendanceDTO> onTimeResult = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), null, AttendanceStatusFilter.ON_TIME, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(onTimeResult.getTotalElements()).isEqualTo(1);
        assertThat(onTimeResult.getContent().get(0).getStatus()).isEqualTo(AttendanceStatus.ON_TIME);
    }

    @Test
    void testGetDailyUserAttendancesWithUserNameFilter() throws IOException {
        // Given
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant todayInstant = todayStart.plusHours(9).toInstant();

        Attendance attendance1 = attendanceRepository.save(Attendance.builder()
                .user(user1) 
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance attendance2 = attendanceRepository.save(Attendance.builder()
                .user(user2) 
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        checkInRepository.save(TestDataHelper.createCheckIn(attendance1, todayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance2, todayInstant));

        // When - Search by partial name that matches the first user's firstName
        Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), null, null, user1.getFirstName().toLowerCase(), null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(user1.getId());
    }

    @Test
    void testGetDailyUserAttendancesWithDepartmentFilter() throws IOException {
        // Given
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant todayInstant = todayStart.plusHours(9).toInstant();

        Attendance attendance1 = attendanceRepository.save(Attendance.builder()
                .user(user1) // Engineering dept
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance attendance2 = attendanceRepository.save(Attendance.builder()
                .user(user3) // Sales dept
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        checkInRepository.save(TestDataHelper.createCheckIn(attendance1, todayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance2, todayInstant));

        // When - Filter by Engineering department
        Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), null, null, null, 
                Collections.singletonList(department1.getId()), PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDepartmentName()).isEqualTo("Engineering");
    }

    @Test
    void testGetDailyUserAttendancesExcludesYesterdayData() throws IOException {
        // Given
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        LocalDate yesterday = today.minusDays(1);
        
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        ZonedDateTime yesterdayStart = yesterday.atStartOfDay(zoneId);
        
        Instant todayInstant = todayStart.plusHours(9).toInstant();
        Instant yesterdayInstant = yesterdayStart.plusHours(9).toInstant();

        // Create yesterday's attendance
        Attendance yesterdayAttendance = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(yesterdayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        // Create today's attendance
        Attendance todayAttendance = attendanceRepository.save(Attendance.builder()
                .user(user2)
                .date(todayInstant)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        checkInRepository.save(TestDataHelper.createCheckIn(yesterdayAttendance, yesterdayInstant));
        checkInRepository.save(TestDataHelper.createCheckIn(todayAttendance, todayInstant));

        // When - Query for today's data
        Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                organization.getId(), null, null, null, null, PageRequest.of(0, 10));

        // Then - Should only get today's attendance
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(user2.getId());
    }
}