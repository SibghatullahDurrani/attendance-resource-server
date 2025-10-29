package com.main.face_recognition_resource_server.services.attendance.reports;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyUserAttendanceDTO;
import com.main.face_recognition_resource_server.TestDataHelper;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.domains.*;
import com.main.face_recognition_resource_server.repositories.*;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.repositories.shift.ShiftRepository;
import com.main.face_recognition_resource_server.repositories.user.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DateRangeSpecificationTest {

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

    private Organization organization;
    private Department department;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Clean up
        checkInRepository.deleteAll();
        checkOutRepository.deleteAll();
        attendanceRepository.deleteAll();
        
        // Create test data
        OrganizationPolicies policies = TestDataHelper.createOrganizationPolicies();
        organization = organizationRepository.save(TestDataHelper.createOrganization(policies));
        
        department = Department.builder()
                .departmentName("Test Department")
                .organization(organization)
                .build();
        department = departmentRepository.save(department);

        Shift shift = shiftRepository.save(TestDataHelper.createShift(organization));

        UserShiftSetting setting1 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting2 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());

        user1 = userRepository.save(TestDataHelper.createUser(department, shift, setting1, "user1"));
        user2 = userRepository.save(TestDataHelper.createUser(department, shift, setting2, "user2"));
    }

    @Test
    void testDateRangeSpecificationForToday() throws IOException {
        // Given - Create attendance records for today at different times
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        
        // Create attendance at different times of the day
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant morningTime = todayStart.plusHours(9).toInstant(); // 9 AM
        Instant afternoonTime = todayStart.plusHours(14).toInstant(); // 2 PM
        
        // Create attendance records
        Attendance attendance1 = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(morningTime)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance attendance2 = attendanceRepository.save(Attendance.builder()
                .user(user2)
                .date(afternoonTime)
                .status(AttendanceStatus.LATE)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        // Create CheckIn records
        checkInRepository.save(TestDataHelper.createCheckIn(attendance1, morningTime));
        checkInRepository.save(TestDataHelper.createCheckIn(attendance2, afternoonTime));

        // When - Create specification with date range for today
        Specification<Attendance> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);
            Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);
            Join<Department, Organization> attendanceUserDepartmentOrganizationJoin = attendanceUserDepartmentJoin.join("organization", JoinType.INNER);

            // Calculate today's date range
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
            ZonedDateTime zonedStartOfDay = startOfDay.atZone(zoneId);
            ZonedDateTime zonedEndOfDay = endOfDay.atZone(zoneId);
            Instant startOfDayInstant = zonedStartOfDay.toInstant();
            Instant endOfDayInstant = zonedEndOfDay.toInstant();

            // Use date range instead of exact match
            predicates.add(criteriaBuilder.equal(attendanceUserDepartmentOrganizationJoin.get("id"), organization.getId()));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startOfDayInstant));
            predicates.add(criteriaBuilder.lessThan(root.get("date"), endOfDayInstant));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<DailyUserAttendanceDTO> result = attendanceRepository.getDailyUserAttendances(specification, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void testDateRangeExcludesYesterdayAndTomorrow() throws IOException {
        // Given - Create attendance for yesterday, today, and tomorrow
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        
        ZonedDateTime yesterdayTime = yesterday.atStartOfDay(zoneId).plusHours(9);
        ZonedDateTime todayTime = today.atStartOfDay(zoneId).plusHours(9);
        ZonedDateTime tomorrowTime = tomorrow.atStartOfDay(zoneId).plusHours(9);
        
        // Create attendance records
        Attendance yesterdayAttendance = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(yesterdayTime.toInstant())
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance todayAttendance = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(todayTime.toInstant())
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance tomorrowAttendance = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(tomorrowTime.toInstant())
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        // Create CheckIn records
        checkInRepository.save(TestDataHelper.createCheckIn(yesterdayAttendance, yesterdayTime.toInstant()));
        checkInRepository.save(TestDataHelper.createCheckIn(todayAttendance, todayTime.toInstant()));
        checkInRepository.save(TestDataHelper.createCheckIn(tomorrowAttendance, tomorrowTime.toInstant()));

        // When - Create specification for today only
        Specification<Attendance> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);
            Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);
            Join<Department, Organization> attendanceUserDepartmentOrganizationJoin = attendanceUserDepartmentJoin.join("organization", JoinType.INNER);

            // Calculate today's date range
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
            ZonedDateTime zonedStartOfDay = startOfDay.atZone(zoneId);
            ZonedDateTime zonedEndOfDay = endOfDay.atZone(zoneId);
            Instant startOfDayInstant = zonedStartOfDay.toInstant();
            Instant endOfDayInstant = zonedEndOfDay.toInstant();

            // Use date range for today only
            predicates.add(criteriaBuilder.equal(attendanceUserDepartmentOrganizationJoin.get("id"), organization.getId()));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startOfDayInstant));
            predicates.add(criteriaBuilder.lessThan(root.get("date"), endOfDayInstant));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<DailyUserAttendanceDTO> result = attendanceRepository.getDailyUserAttendances(specification, PageRequest.of(0, 10));

        // Then - Should only return today's attendance
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(user1.getId());
    }

    @Test
    void testDateRangeHandlesEdgeCases() throws IOException {
        // Given - Create attendance at edge times (start and end of day)
        String timeZone = organization.getTimeZone();
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zoneId);
        
        ZonedDateTime todayStart = today.atStartOfDay(zoneId);
        Instant startOfDayExact = todayStart.toInstant();
        Instant justBeforeMidnight = todayStart.plusDays(1).minusSeconds(1).toInstant();
        
        // Create attendance at exact start of day and just before end
        Attendance morningAttendance = attendanceRepository.save(Attendance.builder()
                .user(user1)
                .date(startOfDayExact)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build());

        Attendance eveningAttendance = attendanceRepository.save(Attendance.builder()
                .user(user2)
                .date(justBeforeMidnight)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_OUT)
                .build());

        // Create CheckIn records
        checkInRepository.save(TestDataHelper.createCheckIn(morningAttendance, startOfDayExact));
        checkInRepository.save(TestDataHelper.createCheckIn(eveningAttendance, justBeforeMidnight));

        // When - Query with date range
        Specification<Attendance> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);
            Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);
            Join<Department, Organization> attendanceUserDepartmentOrganizationJoin = attendanceUserDepartmentJoin.join("organization", JoinType.INNER);

            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
            ZonedDateTime zonedStartOfDay = startOfDay.atZone(zoneId);
            ZonedDateTime zonedEndOfDay = endOfDay.atZone(zoneId);
            Instant startOfDayInstant = zonedStartOfDay.toInstant();
            Instant endOfDayInstant = zonedEndOfDay.toInstant();

            predicates.add(criteriaBuilder.equal(attendanceUserDepartmentOrganizationJoin.get("id"), organization.getId()));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startOfDayInstant));
            predicates.add(criteriaBuilder.lessThan(root.get("date"), endOfDayInstant));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<DailyUserAttendanceDTO> result = attendanceRepository.getDailyUserAttendances(specification, PageRequest.of(0, 10));

        // Then - Both should be included
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }
}