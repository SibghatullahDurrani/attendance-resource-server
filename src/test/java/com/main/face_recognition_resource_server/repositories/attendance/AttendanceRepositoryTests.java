package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.FlatAttendanceExcelChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import com.main.face_recognition_resource_server.TestDataHelper;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.domains.*;
import com.main.face_recognition_resource_server.projections.attendance.CalendarAttendanceProjection;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import com.main.face_recognition_resource_server.repositories.UserShiftRepository;
import com.main.face_recognition_resource_server.repositories.shift.ShiftRepository;
import com.main.face_recognition_resource_server.repositories.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Comprehensive AttendanceRepository Tests")
public class AttendanceRepositoryTests {

    @Autowired
    private AttendanceRepository attendanceRepository;
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
    private CheckInRepository checkInRepository;
    @Autowired
    private CheckOutRepository checkOutRepository;

    // Test data
    private Organization org1, org2;
    private Department dept1Org1, dept2Org1, dept1Org2;
    private User user1Dept1Org1, user2Dept1Org1, user3Dept1Org1, user4Dept1Org1;
    private User user1Dept2Org1, user1Dept1Org2;

    private static Stream<Arguments> dateRangeProvider() {
        return Stream.of(Arguments.of(Instant.now().truncatedTo(ChronoUnit.DAYS), Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS), "Single day range"), Arguments.of(Instant.now().truncatedTo(ChronoUnit.DAYS), Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS), "Month range"), Arguments.of(Instant.now().truncatedTo(ChronoUnit.DAYS), Instant.now().truncatedTo(ChronoUnit.DAYS).plus(365, ChronoUnit.DAYS), "Year range"));
    }

    @BeforeAll
    void setupTestData() {
        // Organizations
        org1 = organizationRepository.save(TestDataHelper.createOrganization(TestDataHelper.createOrganizationPolicies()));
        org2 = organizationRepository.save(TestDataHelper.createOrganization(TestDataHelper.createOrganizationPolicies()));

        // Departments
        dept1Org1 = departmentRepository.save(TestDataHelper.createDepartment(org1));
        dept2Org1 = departmentRepository.save(TestDataHelper.createDepartment(org1));
        dept1Org2 = departmentRepository.save(TestDataHelper.createDepartment(org2));

        // Shifts
        Shift shift1 = shiftRepository.save(TestDataHelper.createShift(org1));
        Shift shift2 = shiftRepository.save(TestDataHelper.createShift(org2));

        // UserShiftSettings
        UserShiftSetting setting1 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting2 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting3 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting4 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting5 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());
        UserShiftSetting setting6 = userShiftRepository.save(TestDataHelper.createUserShiftSetting());

        // Users
        user1Dept1Org1 = userRepository.save(TestDataHelper.createUser(dept1Org1, shift1, setting1, "user1"));
        user2Dept1Org1 = userRepository.save(TestDataHelper.createUser(dept1Org1, shift1, setting2, "user2"));
        user3Dept1Org1 = userRepository.save(TestDataHelper.createUser(dept1Org1, shift1, setting3, "user3"));
        user1Dept2Org1 = userRepository.save(TestDataHelper.createUser(dept2Org1, shift1, setting4, "user4"));
        user1Dept1Org2 = userRepository.save(TestDataHelper.createUser(dept1Org2, shift2, setting5, "user5"));
        user4Dept1Org1 = userRepository.save(TestDataHelper.createUser(dept1Org1, shift1, setting6, "user6"));
    }

    @AfterEach
    void cleanup() {
        checkInRepository.deleteAll();
        checkOutRepository.deleteAll();
        attendanceRepository.deleteAll();
        attendanceRepository.flush();
    }

    // Helper methods
    private void createAttendanceRecords(User user, Instant startDate, int count) {
        for (int i = 0; i < count; i++) {
            Attendance attendance = TestDataHelper.createAttendance(user, startDate.plus(i, ChronoUnit.DAYS));
            attendanceRepository.save(attendance);
        }
    }

    private Attendance createAttendanceWithStatus(User user, Instant date, AttendanceStatus status) {
        Attendance attendance = TestDataHelper.createAttendance(user, date);
        attendance.setStatus(status);
        return attendanceRepository.save(attendance);
    }

    private List<Attendance> createMixedStatusAttendance(User user, Instant startDate, int days) {
        List<Attendance> attendances = new ArrayList<>();
        AttendanceStatus[] statuses = AttendanceStatus.values();

        for (int i = 0; i < days; i++) {
            AttendanceStatus status = statuses[i % statuses.length];
            attendances.add(createAttendanceWithStatus(user, startDate.plus(i, ChronoUnit.DAYS), status));
        }
        return attendances;
    }

    @ParameterizedTest
    @MethodSource("dateRangeProvider")
    @DisplayName("Should handle various date ranges")
    void shouldHandleVariousDateRanges(Instant startDate, Instant endDate, String description) {
        // Given
        createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
        createAttendanceWithStatus(user1Dept1Org1, endDate, AttendanceStatus.LATE);

        // When
        List<UserAttendanceDTO> result = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(startDate, endDate.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

        // Then
        assertThat(result).hasSize(2).as("Should find 2 attendance records for %s", description);
    }

    @Nested
    @DisplayName("getAttendanceByUserIdAndDate Tests")
    class GetAttendanceByUserIdAndDateTests {

        @Test
        @DisplayName("Should retrieve attendance with exact time boundaries")
        void shouldRetrieveAttendanceWithExactBoundaries() {
            Instant exactTime = Instant.parse("2024-01-15T09:30:00Z");
            Instant startOfDay = Instant.parse("2024-01-15T00:00:00Z");
            Instant endOfDay = Instant.parse("2024-01-16T00:00:00Z");

            Attendance attendance = TestDataHelper.createAttendance(user1Dept1Org1, exactTime);
            Attendance saved = attendanceRepository.save(attendance);

            Optional<Attendance> result = attendanceRepository.getAttendanceByUserIdAndDate(
                    user1Dept1Org1.getId(), startOfDay, endOfDay);

            assertThat(result)
                    .isPresent()
                    .hasValueSatisfying(a -> {
                        assertThat(a.getId()).isEqualTo(saved.getId());
                        assertThat(a.getDate()).isEqualTo(exactTime);
                        assertThat(a.getUser().getId()).isEqualTo(user1Dept1Org1.getId());
                        assertThat(a.getStatus()).isEqualTo(saved.getStatus());
                    });
        }

        @Test
        @DisplayName("Should handle millisecond precision correctly")
        void shouldHandleMillisecondPrecision() {
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant timeWithMillis = baseTime.plusMillis(999);

            attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, timeWithMillis));

            Optional<Attendance> included = attendanceRepository.getAttendanceByUserIdAndDate(
                    user1Dept1Org1.getId(), baseTime, baseTime.plusSeconds(1));
            Optional<Attendance> excluded = attendanceRepository.getAttendanceByUserIdAndDate(
                    user1Dept1Org1.getId(), baseTime, baseTime.plusMillis(999));

            assertThat(included).isPresent();
            assertThat(excluded).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
                "0, 1, true",    // Start of day to next day
                "0, 0, false",   // Same instant (empty range)
                "-1, 1, true",   // Previous day to next day
                "-1, 0, false"
        })
        @DisplayName("Should handle various date ranges correctly")
        void shouldHandleVariousDateRanges(int startOffsetDays, int endOffsetDays, boolean shouldFind) {
            Instant attendanceTime = Instant.now().truncatedTo(ChronoUnit.DAYS);
            attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, attendanceTime));

            Optional<Attendance> result = attendanceRepository.getAttendanceByUserIdAndDate(
                    user1Dept1Org1.getId(),
                    attendanceTime.plus(startOffsetDays, ChronoUnit.DAYS),
                    attendanceTime.plus(endOffsetDays, ChronoUnit.DAYS));

            assertThat(result.isPresent()).isEqualTo(shouldFind);
        }

        @Test
        @DisplayName("Should handle multiple attendances in range")
        void shouldHandleMultipleAttendancesInRange() {
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);

            attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, day1));
            attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, day2));

            Optional<Attendance> result = attendanceRepository.getAttendanceByUserIdAndDate(
                    user1Dept1Org1.getId(), day1, day2);

            assertThat(result).isPresent();
            assertThat(result.get().getDate()).isEqualTo(day1);
        }

        @Test
        @DisplayName("Should handle timezone boundaries correctly")
        void shouldHandleTimezoneBoundaries() {
            Instant midnightUTC = LocalDate.of(2024, 1, 15)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();

            attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, midnightUTC));

            Instant startEST = LocalDate.of(2024, 1, 14)
                    .atTime(19, 0)
                    .atZone(ZoneId.of("America/New_York")).toInstant();
            Instant endEST = startEST.plus(1, ChronoUnit.DAYS);

            Optional<Attendance> result = attendanceRepository.getAttendanceByUserIdAndDate(
                    user1Dept1Org1.getId(), startEST, endEST);

            assertThat(result).isPresent();
            assertThat(result.get().getDate()).isEqualTo(midnightUTC);
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L})
        @DisplayName("Should handle invalid user IDs")
        void shouldHandleInvalidUserIds(Long userId) {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

            Optional<Attendance> result = attendanceRepository.getAttendanceByUserIdAndDate(
                    userId, startDate, endDate);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("attendanceExistsByOrganizationIdAndDate Tests")
    class AttendanceExistsByOrganizationIdAndDateTests {

        @Test
        @DisplayName("Should identify existence with multiple statuses")
        void shouldIdentifyExistenceWithMultipleStatuses() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.LATE);
            createAttendanceWithStatus(user3Dept1Org1, date, AttendanceStatus.ABSENT);

            boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), date);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should exclude ON_LEAVE status")
        void shouldExcludeOnLeaveStatus() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_LEAVE);
            createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.ON_LEAVE);

            boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), date);

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should handle mixed statuses correctly")
        void shouldHandleMixedStatuses() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_LEAVE);
            createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.ON_TIME);

            boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), date);

            assertThat(exists).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = AttendanceStatus.class)
        @DisplayName("Should handle each attendance status correctly")
        void shouldHandleEachAttendanceStatus(AttendanceStatus status) {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);
            createAttendanceWithStatus(user1Dept1Org1, date, status);

            boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), date);

            boolean expectedResult = status != AttendanceStatus.ON_LEAVE;
            assertThat(exists).isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("Should handle date boundary precisely")
        void shouldHandleDateBoundaryPrecisely() {
            Instant dateAtMidnight = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant justBeforeMidnight = dateAtMidnight.minusMillis(1);
            Instant justAfterMidnight = dateAtMidnight.plusMillis(1);

            createAttendanceWithStatus(user1Dept1Org1, justBeforeMidnight, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, dateAtMidnight, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user3Dept1Org1, justAfterMidnight, AttendanceStatus.ON_TIME);

            boolean existsBeforeMidnight = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), justBeforeMidnight);
            boolean existsAtMidnight = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), dateAtMidnight);
            boolean existsAfterMidnight = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), justAfterMidnight);

            assertThat(existsBeforeMidnight).isTrue();
            assertThat(existsAtMidnight).isTrue();
            assertThat(existsAfterMidnight).isTrue();
        }

        @Test
        @DisplayName("Should handle cross-department queries")
        void shouldHandleCrossDepartmentQueries() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept2Org1, date, AttendanceStatus.LATE);

            boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    org1.getId(), date);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existent organization")
        void shouldReturnFalseForNonExistentOrganization() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);
            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            Long nonExistentOrgId = 999999L;

            boolean exists = attendanceRepository.attendanceExistsByOrganizationIdAndDate(
                    nonExistentOrgId, date);

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("getAttendanceIdsOfUserBetweenDates Tests")
    class GetAttendanceIdsOfUserBetweenDatesTests {

        @Test
        @DisplayName("Should return IDs in chronological order")
        void shouldReturnIdsInChronologicalOrder() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            List<Long> createdIds = new ArrayList<>();

            List<Integer> dayOffsets = Arrays.asList(2, 0, 4, 1, 3);
            for (Integer offset : dayOffsets) {
                Attendance attendance = attendanceRepository.save(
                        TestDataHelper.createAttendance(user1Dept1Org1,
                                startDate.plus(offset, ChronoUnit.DAYS)));
                createdIds.add(attendance.getId());
            }

            List<Long> retrievedIds = attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    startDate, startDate.plus(5, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(retrievedIds).hasSize(createdIds.size());
            for (int i = 0; i < retrievedIds.size() - 1; i++) {
                Attendance current = attendanceRepository.findById(retrievedIds.get(i)).orElseThrow();
                Attendance next = attendanceRepository.findById(retrievedIds.get(i + 1)).orElseThrow();
                assertThat(current.getDate()).isBefore(next.getDate());
            }
        }

        @Test
        @DisplayName("Should handle exclusive end date correctly")
        void shouldHandleExclusiveEndDate() {
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);
            Instant day3 = day1.plus(2, ChronoUnit.DAYS);

            Attendance att1 = attendanceRepository.save(
                    TestDataHelper.createAttendance(user1Dept1Org1, day1));
            attendanceRepository.save(
                    TestDataHelper.createAttendance(user1Dept1Org1, day2));
            attendanceRepository.save(
                    TestDataHelper.createAttendance(user1Dept1Org1, day3));

            List<Long> ids = attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    day1, day2, user1Dept1Org1.getId());

            assertThat(ids).containsExactly(att1.getId());
        }

        @Test
        @DisplayName("Should handle large date ranges efficiently")
        void shouldHandleLargeDateRangesEfficiently() {
            Instant startDate = LocalDate.of(2024, 1, 1)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();

            List<Attendance> yearAttendances = new ArrayList<>();
            for (int i = 0; i < 365; i++) {
                yearAttendances.add(TestDataHelper.createAttendance(
                        user1Dept1Org1, startDate.plus(i, ChronoUnit.DAYS)));
            }
            attendanceRepository.saveAll(yearAttendances);

            long startTime = System.currentTimeMillis();
            List<Long> ids = attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    startDate, startDate.plus(365, ChronoUnit.DAYS), user1Dept1Org1.getId());
            long queryTime = System.currentTimeMillis() - startTime;

            assertThat(ids).hasSize(365);
            assertThat(queryTime).isLessThan(1000);
        }

        @Test
        @DisplayName("Should return empty list for various edge cases")
        void shouldReturnEmptyListForEdgeCases() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);
            attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, date));

            assertThat(attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    date.plus(1, ChronoUnit.DAYS),
                    date.plus(2, ChronoUnit.DAYS),
                    user1Dept1Org1.getId()
            )).isEmpty();

            assertThat(attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    date.minus(2, ChronoUnit.DAYS),
                    date.minus(1, ChronoUnit.DAYS),
                    user1Dept1Org1.getId()
            )).isEmpty();

            assertThat(attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    date.plus(1, ChronoUnit.DAYS),
                    date,
                    user1Dept1Org1.getId()
            )).isEmpty();

            assertThat(attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    date,
                    date.plus(1, ChronoUnit.DAYS),
                    999999L
            )).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L})
        @DisplayName("Should handle invalid user IDs gracefully")
        void shouldHandleInvalidUserIds(Long userId) {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

            List<Long> ids = attendanceRepository.getAttendanceIdsOfUserBetweenDates(
                    startDate, endDate, userId);

            assertThat(ids).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDepartmentsAttendanceExcelData Tests")
    class GetDepartmentsAttendanceExcelDataTests {

        @Test
        @DisplayName("Should get attendance excel data for multiple departments")
        void shouldGetAttendanceExcelDataForMultipleDepartments() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, startDate, 3);
            createAttendanceRecords(user1Dept2Org1, startDate, 2);
            createAttendanceRecords(user1Dept1Org2, startDate, 1); // Different org - shouldn't be included

            // When
            List<AttendanceExcelDataDTO> result = attendanceRepository.getDepartmentsAttendanceExcelData(startDate, endDate, Arrays.asList(dept1Org1.getId(), dept2Org1.getId()));

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(AttendanceExcelDataDTO::getDivision).containsExactlyInAnyOrder(dept1Org1.getDepartmentName(), dept2Org1.getDepartmentName());
        }

        @Test
        @DisplayName("Should return empty list when no attendance in date range")
        void shouldReturnEmptyListWhenNoAttendanceInDateRange() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);
            Instant outsideDate = startDate.minus(10, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, outsideDate, 3);

            // When
            List<AttendanceExcelDataDTO> result = attendanceRepository.getDepartmentsAttendanceExcelData(startDate, endDate, Collections.singletonList(dept1Org1.getId()));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty department list")
        void shouldHandleEmptyDepartmentList() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            // When
            List<AttendanceExcelDataDTO> result = attendanceRepository.getDepartmentsAttendanceExcelData(startDate, endDate, Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null department IDs in list")
        void shouldHandleNullDepartmentIds() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, startDate, 2);

            // When
            List<AttendanceExcelDataDTO> result = attendanceRepository.getDepartmentsAttendanceExcelData(startDate, endDate, Arrays.asList(dept1Org1.getId(), null));

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getUserExcelAttendance Tests")
    class GetUserExcelAttendanceTests {

        @Test
        @DisplayName("Should get user's attendance records within date range")
        void shouldGetUserAttendanceWithinDateRange() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            List<Attendance> attendances = createMixedStatusAttendance(user1Dept1Org1, startDate, 5);

            // When
            List<ExcelAttendanceDTO> result = attendanceRepository.getUserExcelAttendance(startDate, endDate, user1Dept1Org1.getId());

            // Then
            assertThat(result).hasSize(5);
            assertThat(result).extracting(ExcelAttendanceDTO::getAttendanceStatus).containsExactlyInAnyOrderElementsOf(attendances.stream().map(Attendance::getStatus).toList());
        }

        @Test
        @DisplayName("Should return empty list for user with no attendance")
        void shouldReturnEmptyForUserWithNoAttendance() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            // When
            List<ExcelAttendanceDTO> result = attendanceRepository.getUserExcelAttendance(startDate, endDate, user1Dept1Org1.getId());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle exact date boundaries")
        void shouldHandleExactDateBoundaries() {
            // Given
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            Attendance attendance = attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, date));

            // When
            List<ExcelAttendanceDTO> result = attendanceRepository.getUserExcelAttendance(date, date, user1Dept1Org1.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getAttendanceId()).isEqualTo(attendance.getId());
        }

        @Test
        @DisplayName("Should handle non-existent user ID")
        void shouldHandleNonExistentUserId() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);
            Long nonExistentUserId = 999999L;

            // When
            List<ExcelAttendanceDTO> result = attendanceRepository.getUserExcelAttendance(startDate, endDate, nonExistentUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDepartmentLineChartData Tests")
    class GetDepartmentLineChartDataTests {

        @Test
        @DisplayName("Should aggregate attendance data by department and date")
        void shouldAggregateAttendanceByDepartmentAndDate() {
            // Given
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);
            Instant day3 = day1.plus(2, ChronoUnit.DAYS);

            // Day 1 - Mixed statuses for dept1
            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, day1, AttendanceStatus.LATE);
            createAttendanceWithStatus(user3Dept1Org1, day1, AttendanceStatus.ABSENT);

            // Day 2 - All present for dept1
            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, day2, AttendanceStatus.ON_TIME);

            // Day 1 - dept2
            createAttendanceWithStatus(user1Dept2Org1, day1, AttendanceStatus.ON_LEAVE);

            // When
            List<FlatAttendanceExcelChartDTO> result = attendanceRepository.getDepartmentLineChartData(Arrays.asList(dept1Org1.getId(), dept2Org1.getId()), day1, day3);

            // Then
            assertThat(result).hasSize(3); // 2 days for dept1, 1 day for dept2

            FlatAttendanceExcelChartDTO dept1Day1 = result.stream().filter(r -> r.getDepartmentId().equals(dept1Org1.getId()) && r.getDate().equals(day1)).findFirst().orElseThrow();

            FlatAttendanceExcelChartDTO dep1Day2 = result.stream().filter(r -> r.getDepartmentId().equals(dept1Org1.getId()) && r.getDate().equals(day2)).findFirst().orElseThrow();

            FlatAttendanceExcelChartDTO dep2Day1 = result.stream().filter(r -> r.getDepartmentId().equals(dept2Org1.getId()) && r.getDate().equals(day1)).findFirst().orElseThrow();

            assertThat(dept1Day1.getOnTime()).isEqualTo(1L);
            assertThat(dept1Day1.getLate()).isEqualTo(1L);
            assertThat(dept1Day1.getAbsent()).isEqualTo(1L);
            assertThat(dept1Day1.getOnLeave()).isEqualTo(0L);

            assertThat(dep1Day2.getOnTime()).isEqualTo(2L);
            assertThat(dep1Day2.getLate()).isEqualTo(0L);
            assertThat(dep1Day2.getAbsent()).isEqualTo(0L);
            assertThat(dep1Day2.getOnLeave()).isEqualTo(0L);

            assertThat(dep2Day1.getOnTime()).isEqualTo(0L);
            assertThat(dep2Day1.getOnLeave()).isEqualTo(1L);
            assertThat(dep2Day1.getAbsent()).isEqualTo(0L);
            assertThat(dep2Day1.getLate()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle departments with no attendance")
        void shouldHandleDepartmentsWithNoAttendance() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            // When
            List<FlatAttendanceExcelChartDTO> result = attendanceRepository.getDepartmentLineChartData(Collections.singletonList(dept1Org1.getId()), startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should order results by department and date")
        void shouldOrderByDepartmentAndDate() {
            // Given
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept2Org1, day2, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept2Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.ON_TIME);

            // When
            List<FlatAttendanceExcelChartDTO> result = attendanceRepository.getDepartmentLineChartData(Arrays.asList(dept1Org1.getId(), dept2Org1.getId()), day1, day2.plus(1, ChronoUnit.DAYS));

            // Then
            assertThat(result).hasSize(4);
            // Verify ordering
            assertThat(result.get(0).getDepartmentId()).isEqualTo(dept1Org1.getId());
            assertThat(result.get(0).getDate()).isEqualTo(day1);
            assertThat(result.get(1).getDepartmentId()).isEqualTo(dept1Org1.getId());
            assertThat(result.get(1).getDate()).isEqualTo(day2);
        }
    }

    @Nested
    @DisplayName("getUsersAttendancePieChartData Tests")
    class GetUsersAttendancePieChartDataTests {

        @Test
        @DisplayName("Should aggregate attendance counts per user")
        void shouldAggregateAttendanceCountsPerUser() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(10, ChronoUnit.DAYS);

            // User 1: 5 on-time, 2 late, 1 absent, 1 leave
            for (int i = 0; i < 5; i++) {
                createAttendanceWithStatus(user1Dept1Org1, startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(5, ChronoUnit.DAYS), AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(6, ChronoUnit.DAYS), AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(7, ChronoUnit.DAYS), AttendanceStatus.ABSENT);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(8, ChronoUnit.DAYS), AttendanceStatus.ON_LEAVE);

            // User 2: 3 on-time
            for (int i = 0; i < 3; i++) {
                createAttendanceWithStatus(user2Dept1Org1, startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }

            // When
            List<UserAttendancePieChartDTO> result = attendanceRepository.getUsersAttendancePieChartData(startDate, endDate, Arrays.asList(user1Dept1Org1.getId(), user2Dept1Org1.getId()));

            // Then
            assertThat(result).hasSize(2);

            UserAttendancePieChartDTO user1Data = result.stream().filter(r -> r.getUserId().equals(user1Dept1Org1.getId())).findFirst().orElseThrow();

            assertThat(user1Data.getOnTime()).isEqualTo(7L); // 5 on-time + 2 late
            assertThat(user1Data.getLate()).isEqualTo(2L);
            assertThat(user1Data.getAbsent()).isEqualTo(1L);
            assertThat(user1Data.getOnLeave()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle users with no attendance")
        void shouldExcludeUsersWithNoAttendance() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            // user2 has no attendance

            // When
            List<UserAttendancePieChartDTO> result = attendanceRepository.getUsersAttendancePieChartData(startDate, endDate, Arrays.asList(user1Dept1Org1.getId(), user2Dept1Org1.getId()));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUserId()).isEqualTo(user1Dept1Org1.getId());
        }

        @Test
        @DisplayName("Should handle empty user list")
        void shouldHandleEmptyUserList() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            // When
            List<UserAttendancePieChartDTO> result = attendanceRepository.getUsersAttendancePieChartData(startDate, endDate, Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrganizationAttendanceData Tests")
    class GetOrganizationAttendanceDataTests {

        @Test
        @DisplayName("Should get all users' attendance data for organization")
        void shouldGetAllUsersAttendanceForOrganization() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, startDate, 2);
            createAttendanceRecords(user1Dept2Org1, startDate, 2);
            createAttendanceRecords(user1Dept1Org2, startDate, 2); // Different org

            // When
            List<AttendanceExcelDataDTO> result = attendanceRepository.getOrganizationAttendanceData(startDate, endDate, org1.getId());

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(AttendanceExcelDataDTO::getUserId).containsExactlyInAnyOrder(user1Dept1Org1.getId(), user1Dept2Org1.getId());
        }

        @Test
        @DisplayName("Should exclude users from other organizations")
        void shouldExcludeUsersFromOtherOrganizations() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, startDate, 2);
            createAttendanceRecords(user1Dept1Org2, startDate, 2);

            // When
            List<AttendanceExcelDataDTO> resultOrg1 = attendanceRepository.getOrganizationAttendanceData(startDate, endDate, org1.getId());
            List<AttendanceExcelDataDTO> resultOrg2 = attendanceRepository.getOrganizationAttendanceData(startDate, endDate, org2.getId());

            // Then
            assertThat(resultOrg1).hasSize(1);
            assertThat(resultOrg1.getFirst().getUserId()).isEqualTo(user1Dept1Org1.getId());

            assertThat(resultOrg2).hasSize(1);
            assertThat(resultOrg2.getFirst().getUserId()).isEqualTo(user1Dept1Org2.getId());
        }

        @Test
        @DisplayName("Should handle organization with no attendance")
        void shouldHandleOrganizationWithNoAttendance() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            // When
            List<AttendanceExcelDataDTO> result = attendanceRepository.getOrganizationAttendanceData(startDate, endDate, org1.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrganizationPieChartData Tests")
    class GetOrganizationPieChartDataTests {

        @Test
        @DisplayName("Should aggregate attendance by departments")
        void shouldAggregateAttendanceByDepartments() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            // Dept1: Mixed attendance
            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, startDate, AttendanceStatus.LATE);
            createAttendanceWithStatus(user3Dept1Org1, startDate, AttendanceStatus.ABSENT);

            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.ON_LEAVE);

            // Dept2: Some attendance
            createAttendanceWithStatus(user1Dept2Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept2Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.LATE);

            // When
            List<DepartmentAttendancePieChartDTO> result = attendanceRepository.getOrganizationPieChartData(startDate, endDate, org1.getId());

            // Then
            assertThat(result).hasSize(2);

            DepartmentAttendancePieChartDTO dept1Data = result.stream().filter(r -> r.getDepartmentId().equals(dept1Org1.getId())).findFirst().orElseThrow();

            assertThat(dept1Data.getOnTime()).isEqualTo(3L); // 2 on-time + 1 late
            assertThat(dept1Data.getLate()).isEqualTo(1L);
            assertThat(dept1Data.getAbsent()).isEqualTo(1L);
            assertThat(dept1Data.getOnLeave()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should exclude departments from other organizations")
        void shouldExcludeDepartmentsFromOtherOrganizations() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org2, startDate, AttendanceStatus.ON_TIME);

            // When
            List<DepartmentAttendancePieChartDTO> result = attendanceRepository.getOrganizationPieChartData(startDate, endDate, org1.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getDepartmentId()).isEqualTo(dept1Org1.getId());
        }
    }

    @Nested
    @DisplayName("getDepartmentsAttendancePieChartData Tests")
    class GetDepartmentsAttendancePieChartDataTests {

        @Test
        @DisplayName("Should get pie chart data for specific departments")
        void shouldGetPieChartDataForSpecificDepartments() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept2Org1, startDate, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org2, startDate, AttendanceStatus.ABSENT); // Different org

            // When
            List<DepartmentAttendancePieChartDTO> result = attendanceRepository.getDepartmentsAttendancePieChartData(Arrays.asList(dept1Org1.getId(), dept2Org1.getId(), dept1Org2.getId()), startDate, endDate);

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should handle departments with no data")
        void shouldHandleDepartmentsWithNoData() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            // When
            List<DepartmentAttendancePieChartDTO> result = attendanceRepository.getDepartmentsAttendancePieChartData(Collections.singletonList(dept1Org1.getId()), startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrganizationLineChartData Tests")
    class GetOrganizationLineChartDataTests {

        @Test
        @DisplayName("Should aggregate daily attendance for entire organization")
        void shouldAggregateDailyAttendanceForOrganization() {
            // Given
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);
            Instant day3 = day1.plus(2, ChronoUnit.DAYS);

            // Day 1 - Multiple departments
            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, day1, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept2Org1, day1, AttendanceStatus.ABSENT);

            // Day 2
            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.ON_LEAVE);
            createAttendanceWithStatus(user1Dept2Org1, day2, AttendanceStatus.ON_TIME);

            // Other org (should not be included)
            createAttendanceWithStatus(user1Dept1Org2, day1, AttendanceStatus.ON_TIME);

            // When
            List<FlatAttendanceExcelChartDTO> result = attendanceRepository.getOrganizationLineChartData(org1.getId(), day1, day3);

            // Then
            assertThat(result).hasSize(4); // 2 depts × 2 days with data

            // Verify day 1 dept1 data
            FlatAttendanceExcelChartDTO day1Dept1 = result.stream().filter(r -> r.getDepartmentId().equals(dept1Org1.getId()) && r.getDate().equals(day1)).findFirst().orElseThrow();

            assertThat(day1Dept1.getOnTime()).isEqualTo(2L);
            assertThat(day1Dept1.getLate()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should order by department and date")
        void shouldOrderByDepartmentAndDate() {
            // Given
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);

            // Create attendance in mixed order
            createAttendanceWithStatus(user1Dept2Org1, day2, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept2Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.ON_TIME);

            // When
            List<FlatAttendanceExcelChartDTO> result = attendanceRepository.getOrganizationLineChartData(org1.getId(), day1, day2.plus(1, ChronoUnit.DAYS));

            // Then
            assertThat(result).hasSize(4);
            assertThat(result.get(0).getDepartmentId()).isEqualTo(dept1Org1.getId());
            assertThat(result.get(0).getDate()).isEqualTo(day1);
            assertThat(result.get(1).getDepartmentId()).isEqualTo(dept1Org1.getId());
            assertThat(result.get(1).getDate()).isEqualTo(day2);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

//        @Test
//        @DisplayName("Should handle duplicate attendance for same user and date")
//        void shouldPreventDuplicateAttendance() {
//            // Given
//            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);
//            attendanceRepository.saveAndFlush(TestDataHelper.createAttendance(user1Dept1Org1, date));
//            attendanceRepository.flush();
//
//            // When/Then
//            assertThatThrownBy(() -> attendanceRepository.saveAndFlush(TestDataHelper.createAttendance(user1Dept1Org1, date))).isInstanceOf(DataIntegrityViolationException.class);
//        }

        @Test
        @DisplayName("Should handle year boundary queries")
        void shouldHandleYearBoundaryQueries() {
            // Given
            Instant endOfYear = LocalDate.of(2024, 12, 31).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant startOfNextYear = LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();

            createAttendanceWithStatus(user1Dept1Org1, endOfYear, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, startOfNextYear, AttendanceStatus.LATE);

            // When
            List<UserAttendanceDTO> result = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(endOfYear, startOfNextYear.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should handle DST transitions")
        void shouldHandleDSTTransitions() {
            // Given - DST transition dates (example for US Eastern)
            // Spring forward: March 10, 2024 at 2:00 AM
            Instant beforeDST = LocalDate.of(2024, 3, 9).atStartOfDay(ZoneId.of("America/New_York")).toInstant();
            Instant afterDST = LocalDate.of(2024, 3, 11).atStartOfDay(ZoneId.of("America/New_York")).toInstant();

            createAttendanceWithStatus(user1Dept1Org1, beforeDST, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, afterDST, AttendanceStatus.ON_TIME);

            // When
            List<UserAttendanceDTO> result = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(beforeDST, afterDST.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should handle leap year February 29")
        void shouldHandleLeapYear() {
            // Given
            Instant feb28_2024 = LocalDate.of(2024, 2, 28).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant feb29_2024 = LocalDate.of(2024, 2, 29).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant mar1_2024 = LocalDate.of(2024, 3, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();

            createAttendanceWithStatus(user1Dept1Org1, feb28_2024, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, feb29_2024, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, mar1_2024, AttendanceStatus.ABSENT);

            // When
            List<UserAttendanceDTO> result = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(feb28_2024, mar1_2024.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should handle very large date ranges")
        void shouldHandleLargeDateRanges() {
            // Given - 2 year range
            Instant startDate = LocalDate.of(2023, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant endDate = LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();

            // Create sparse attendance
            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, endDate.minus(1, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);

            // When
            List<UserAttendanceDTO> result = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(startDate, endDate, user1Dept1Org1.getId());

            // Then
            assertThat(result).hasSize(2);
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L})
        @DisplayName("Should handle invalid user IDs")
        void shouldHandleInvalidUserIds(Long userId) {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            // When
            List<UserAttendanceDTO> result = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(startDate, endDate, userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Performance and Large Dataset Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle query with 1000+ attendance records efficiently")
        void shouldHandleLargeDatasetEfficiently() {
            // Given - Create 1000 attendance records
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            List<Attendance> largeDataset = new ArrayList<>();

            for (int i = 0; i < 334; i++) {
                largeDataset.add(TestDataHelper.createAttendance(user1Dept1Org1, startDate.plus(i, ChronoUnit.DAYS)));
                largeDataset.add(TestDataHelper.createAttendance(user2Dept1Org1, startDate.plus(i, ChronoUnit.DAYS)));
                largeDataset.add(TestDataHelper.createAttendance(user3Dept1Org1, startDate.plus(i, ChronoUnit.DAYS)));
            }

            long startTime = System.currentTimeMillis();
            attendanceRepository.saveAll(largeDataset);
            attendanceRepository.flush();

            // When
            List<DailyAttendanceGraphDataDTO> result = attendanceRepository.getOrganizationAttendanceChartInfo(org1.getId(), startDate, startDate.plus(334, ChronoUnit.DAYS));

            long endTime = System.currentTimeMillis();

            // Then
            assertThat(result).hasSize(334);
            assertThat(endTime - startTime).isLessThan(5000); // Query should complete within 5 seconds
        }

        @Test
        @DisplayName("Should efficiently paginate large result sets")
        void shouldEfficientlyPaginateLargeResults() {
            // Given
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            for (int i = 0; i < 100; i++) {
                attendanceRepository.save(TestDataHelper.createAttendance(user1Dept1Org1, startDate.plus(i, ChronoUnit.DAYS)));
            }

            // When
            Page<UserAttendanceDTO> page1 = attendanceRepository.getUserAttendancePageBetweenDate(user1Dept1Org1.getId(), startDate, startDate.plus(100, ChronoUnit.DAYS), PageRequest.of(0, 10));

            Page<UserAttendanceDTO> page2 = attendanceRepository.getUserAttendancePageBetweenDate(user1Dept1Org1.getId(), startDate, startDate.plus(100, ChronoUnit.DAYS), PageRequest.of(1, 10));

            // Then
            assertThat(page1.getTotalElements()).isEqualTo(100);
            assertThat(page1.getTotalPages()).isEqualTo(10);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(10);
            assertThat(page1.getContent().getFirst().getId()).isNotEqualTo(page2.getContent().getFirst().getId());
        }
    }

    @Nested
    @DisplayName("Native Query Tests")
    class NativeQueryTests {

        @Test
        @DisplayName("getUserYearlyAttendanceGraphData should handle all 12 months")
        void shouldHandleAll12Months() {
            // Given - Create attendance for each month of 2024
            for (int month = 1; month <= 12; month++) {
                Instant date = LocalDate.of(2024, month, 15).atStartOfDay(ZoneId.of("UTC")).toInstant();
                createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            }

            // When
            List<MonthlyAttendanceGraphDataDTO> result = attendanceRepository.getUserYearlyAttendanceGraphData(user1Dept1Org1.getId(), 2024);

            // Then
            assertThat(result).hasSize(12);
            for (int i = 0; i < 12; i++) {
                assertThat(result.get(i).getMonth()).isEqualTo(i + 1);
                assertThat(result.get(i).getPresentCount()).isEqualTo(1L);
            }
        }

        @Test
        @DisplayName("getUserYearlyAttendanceGraphData should handle months with no data")
        void shouldHandleMonthsWithNoData() {
            // Given - Create attendance only for January and December
            Instant jan = LocalDate.of(2024, 1, 15).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant dec = LocalDate.of(2024, 12, 15).atStartOfDay(ZoneId.of("UTC")).toInstant();

            createAttendanceWithStatus(user1Dept1Org1, jan, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, dec, AttendanceStatus.LATE);

            // When
            List<MonthlyAttendanceGraphDataDTO> result = attendanceRepository.getUserYearlyAttendanceGraphData(user1Dept1Org1.getId(), 2024);

            // Then
            assertThat(result).hasSize(12);
            assertThat(result.getFirst().getPresentCount()).isEqualTo(1L); // January
            assertThat(result.get(11).getLateCount()).isEqualTo(1L); // December

            // February to November should have 0 counts
            for (int i = 1; i < 11; i++) {
                assertThat(result.get(i).getPresentCount()).isEqualTo(0L);
                assertThat(result.get(i).getAbsentCount()).isEqualTo(0L);
                assertThat(result.get(i).getLateCount()).isEqualTo(0L);
                assertThat(result.get(i).getLeaveCount()).isEqualTo(0L);
            }
        }
    }

    @Nested
    @DisplayName("Criteria Repository Tests")
    class CriteriaRepositoryTests {

        @Test
        @DisplayName("Should filter attendance using specifications")
        void shouldFilterUsingSpecifications() throws IOException {
            // Given
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Attendance attendance1 = createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            Attendance attendance2 = createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.LATE);
            Attendance attendance3 = createAttendanceWithStatus(user3Dept1Org1, date, AttendanceStatus.ABSENT);
            
            // Create CheckIn records for each attendance
            checkInRepository.save(TestDataHelper.createCheckIn(attendance1, date.plusSeconds(3600)));
            checkInRepository.save(TestDataHelper.createCheckIn(attendance2, date.plusSeconds(3700)));
            checkInRepository.save(TestDataHelper.createCheckIn(attendance3, date.plusSeconds(3800)));
            
            // Create CheckOut records for some attendances
            checkOutRepository.save(TestDataHelper.createCheckOut(attendance1, date.plusSeconds(32400)));
            checkOutRepository.save(TestDataHelper.createCheckOut(attendance2, date.plusSeconds(32500)));

            // Create specification for ON_TIME status only
            Specification<Attendance> spec = (root, _, cb) -> cb.equal(root.get("status"), AttendanceStatus.ON_TIME);

            // When
            Page<DailyUserAttendanceDTO> result = attendanceRepository.getDailyUserAttendances(spec, PageRequest.of(0, 10));

            // Then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getStatus()).isEqualTo(AttendanceStatus.ON_TIME);
            assertThat(result.getContent().getFirst().getFirstCheckInTime()).isNotNull();
            assertThat(result.getContent().getFirst().getFirstCheckInTime()).isGreaterThan(0L);
            assertThat(result.getContent().getFirst().getLastCheckOutTime()).isNotNull();
            assertThat(result.getContent().getFirst().getLastCheckOutTime()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should combine multiple specifications")
        void shouldCombineMultipleSpecifications() throws IOException {
            // Given
            Instant date1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant date2 = date1.plus(1, ChronoUnit.DAYS);

            Attendance attendance1 = createAttendanceWithStatus(user1Dept1Org1, date1, AttendanceStatus.ON_TIME);
            Attendance attendance2 = createAttendanceWithStatus(user1Dept1Org1, date2, AttendanceStatus.LATE);
            Attendance attendance3 = createAttendanceWithStatus(user2Dept1Org1, date1, AttendanceStatus.ON_TIME);
            
            // Create CheckIn records for each attendance
            checkInRepository.save(TestDataHelper.createCheckIn(attendance1, date1.plusSeconds(3600)));
            checkInRepository.save(TestDataHelper.createCheckIn(attendance2, date2.plusSeconds(3700)));
            checkInRepository.save(TestDataHelper.createCheckIn(attendance3, date1.plusSeconds(3800)));
            
            // Create CheckOut records
            checkOutRepository.save(TestDataHelper.createCheckOut(attendance1, date1.plusSeconds(32400)));
            checkOutRepository.save(TestDataHelper.createCheckOut(attendance2, date2.plusSeconds(32500)));
            checkOutRepository.save(TestDataHelper.createCheckOut(attendance3, date1.plusSeconds(32600)));

            // Create combined specification
            Specification<Attendance> userSpec = (root, _, cb) -> cb.equal(root.get("user").get("id"), user1Dept1Org1.getId());

            Specification<Attendance> statusSpec = (root, _, cb) -> cb.equal(root.get("status"), AttendanceStatus.ON_TIME);

            Specification<Attendance> combinedSpec = userSpec.and(statusSpec);

            // When
            Page<DailyUserAttendanceDTO> result = attendanceRepository.getDailyUserAttendances(combinedSpec, PageRequest.of(0, 10));

            // Then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getUserId()).isEqualTo(user1Dept1Org1.getId());
            assertThat(result.getContent().getFirst().getStatus()).isEqualTo(AttendanceStatus.ON_TIME);
            assertThat(result.getContent().getFirst().getFirstCheckInTime()).isNotNull();
            assertThat(result.getContent().getFirst().getFirstCheckInTime()).isGreaterThan(0L);
            assertThat(result.getContent().getFirst().getLastCheckOutTime()).isNotNull();
            assertThat(result.getContent().getFirst().getLastCheckOutTime()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should get organization monthly user attendances")
        void shouldGetOrganizationMonthlyUserAttendances() {
            // Given
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);
            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept2Org1, date, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org2, date, AttendanceStatus.ABSENT);

            // Create specification for org1
            Specification<Attendance> spec = (root, _, cb) -> cb.equal(root.get("user").get("department").get("organization").get("id"), org1.getId());

            // When
            Page<OrganizationUserAttendanceDTO> result = attendanceRepository.getOrganizationMonthlyUserAttendances(spec, PageRequest.of(0, 10));

            // Then
            assertThat(result.getTotalElements()).isEqualTo(2);
        }
    }


    @Nested
    @DisplayName("getCalendarAttendanceProjectionOfUserBetweenDates Tests")
    class GetCalendarAttendanceProjectionOfUserBetweenDatesTests {

        @Test
        @DisplayName("Should project correct fields with all attendance statuses")
        void shouldProjectCorrectFieldsWithAllStatuses() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Map<Instant, AttendanceStatus> attendanceMap = new HashMap<>();

            int index = 0;
            for (AttendanceStatus status : AttendanceStatus.values()) {
                Instant date = startDate.plus(index++, ChronoUnit.DAYS);
                createAttendanceWithStatus(user1Dept1Org1, date, status);
                attendanceMap.put(date, status);
            }

            List<CalendarAttendanceProjection> projections =
                    attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(
                            startDate,
                            startDate.plus(AttendanceStatus.values().length, ChronoUnit.DAYS),
                            user1Dept1Org1.getId());

            assertThat(projections).hasSize(AttendanceStatus.values().length);
            projections.forEach(projection -> {
                AttendanceStatus expectedStatus = attendanceMap.get(projection.getDate());
                assertThat(projection.getStatus()).isEqualTo(expectedStatus);
            });
        }

        @Test
        @DisplayName("Should maintain chronological order in projections")
        void shouldMaintainChronologicalOrder() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            for (int i = 9; i >= 0; i--) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }

            List<CalendarAttendanceProjection> projections =
                    attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(
                            startDate, startDate.plus(10, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(projections).hasSize(10);
            for (int i = 0; i < projections.size() - 1; i++) {
                assertThat(projections.get(i).getDate())
                        .isBefore(projections.get(i + 1).getDate());
            }
        }

        @Test
        @DisplayName("Should handle sparse attendance data")
        void shouldHandleSparseAttendanceData() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Set<Integer> attendanceDays = Set.of(0, 3, 7, 14, 29);

            for (Integer day : attendanceDays) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(day, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }

            List<CalendarAttendanceProjection> projections =
                    attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(
                            startDate, startDate.plus(30, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(projections).hasSize(attendanceDays.size());
            for (int i = 0; i < projections.size() - 1; i++) {
                long daysBetween = ChronoUnit.DAYS.between(
                        projections.get(i).getDate(),
                        projections.get(i + 1).getDate());
                assertThat(daysBetween).isGreaterThan(1);
            }
        }

        @Test
        @DisplayName("Should handle empty date range")
        void shouldHandleEmptyDateRange() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            List<CalendarAttendanceProjection> projections =
                    attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(
                            date, date, user1Dept1Org1.getId());

            assertThat(projections).isEmpty();
        }

        @Test
        @DisplayName("Should handle non-existent user")
        void shouldHandleNonExistentUser() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            List<CalendarAttendanceProjection> projections =
                    attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(
                            startDate, endDate, 999999L);

            assertThat(projections).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAttendanceOverviewOfUserBetweenDates Tests")
    class GetAttendanceOverviewOfUserBetweenDatesTests {

        @Test
        @DisplayName("Should return correct DTOs with all fields")
        void shouldReturnCorrectDTOsWithAllFields() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            List<Attendance> attendances = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Attendance attendance = createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
                attendances.add(attendance);
            }

            List<UserAttendanceDTO> dtos = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(
                    startDate, startDate.plus(5, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(dtos).hasSize(5);
            for (int i = 0; i < dtos.size(); i++) {
                UserAttendanceDTO dto = dtos.get(i);
                Attendance expected = attendances.get(i);
                assertThat(dto.getId()).isEqualTo(expected.getId());
                assertThat(dto.getDate()).isEqualTo(expected.getDate().toEpochMilli());
                assertThat(dto.getStatus()).isEqualTo(expected.getStatus());
            }
        }

        @Test
        @DisplayName("Should handle different attendance statuses")
        void shouldHandleDifferentAttendanceStatuses() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(2, ChronoUnit.DAYS), AttendanceStatus.ABSENT);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(3, ChronoUnit.DAYS), AttendanceStatus.ON_LEAVE);

            List<UserAttendanceDTO> dtos = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(
                    startDate, startDate.plus(4, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(dtos).hasSize(4);
            assertThat(dtos.stream().map(UserAttendanceDTO::getStatus))
                    .containsExactlyInAnyOrder(AttendanceStatus.ON_TIME, AttendanceStatus.LATE,
                            AttendanceStatus.ABSENT, AttendanceStatus.ON_LEAVE);
        }

        @Test
        @DisplayName("Should handle empty results")
        void shouldHandleEmptyResults() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            List<UserAttendanceDTO> dtos = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(
                    startDate, endDate, user1Dept1Org1.getId());

            assertThat(dtos).isEmpty();
        }

        @Test
        @DisplayName("Should handle date range boundaries")
        void shouldHandleDateRangeBoundaries() {
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);
            Instant day3 = day1.plus(2, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, day3, AttendanceStatus.ABSENT);

            List<UserAttendanceDTO> dtos = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(
                    day1, day3, user1Dept1Org1.getId());

            assertThat(dtos).hasSize(2);
            assertThat(dtos.stream().map(dto -> Instant.ofEpochMilli(dto.getDate())))
                    .containsExactlyInAnyOrder(day1, day2);
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            List<UserAttendanceDTO> dtos = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(
                    startDate, endDate, 999999L);

            assertThat(dtos).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAttendanceIdOfUserBetweenDates Tests")
    class GetAttendanceIdOfUserBetweenDatesTests {

        @Test
        @DisplayName("Should return first attendance ID in range")
        void shouldReturnFirstAttendanceIdInRange() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            Attendance first = createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            Attendance second = createAttendanceWithStatus(user1Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.LATE);

            Optional<Long> result = attendanceRepository.getAttendanceIdOfUserBetweenDates(
                    startDate, startDate.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(first.getId());
        }

        @Test
        @DisplayName("Should return empty for no attendance in range")
        void shouldReturnEmptyForNoAttendanceInRange() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate.minus(1, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);

            Optional<Long> result = attendanceRepository.getAttendanceIdOfUserBetweenDates(
                    startDate, startDate.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single day range")
        void shouldHandleSingleDayRange() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            Attendance attendance = createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);

            Optional<Long> result = attendanceRepository.getAttendanceIdOfUserBetweenDates(
                    date, date.plus(1, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(attendance.getId());
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            Optional<Long> result = attendanceRepository.getAttendanceIdOfUserBetweenDates(
                    startDate, endDate, 999999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAttendanceStatusOfAttendance Tests")
    class GetAttendanceStatusOfAttendanceTests {

        @Test
        @DisplayName("Should return correct status for valid attendance ID")
        void shouldReturnCorrectStatusForValidAttendanceId() {
            Attendance attendance = createAttendanceWithStatus(user1Dept1Org1,
                    Instant.now().truncatedTo(ChronoUnit.DAYS), AttendanceStatus.LATE);

            Optional<AttendanceStatus> result = attendanceRepository.getAttendanceStatusOfAttendance(
                    attendance.getId());

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(AttendanceStatus.LATE);
        }

        @Test
        @DisplayName("Should return empty for non-existent attendance ID")
        void shouldReturnEmptyForNonExistentAttendanceId() {
            Optional<AttendanceStatus> result = attendanceRepository.getAttendanceStatusOfAttendance(999999L);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = AttendanceStatus.class)
        @DisplayName("Should handle all attendance statuses")
        void shouldHandleAllAttendanceStatuses(AttendanceStatus status) {
            Attendance attendance = createAttendanceWithStatus(user1Dept1Org1,
                    Instant.now().truncatedTo(ChronoUnit.DAYS), status);

            Optional<AttendanceStatus> result = attendanceRepository.getAttendanceStatusOfAttendance(
                    attendance.getId());

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(status);
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L})
        @DisplayName("Should handle invalid attendance IDs")
        void shouldHandleInvalidAttendanceIds(Long attendanceId) {
            Optional<AttendanceStatus> result = attendanceRepository.getAttendanceStatusOfAttendance(attendanceId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserAttendancePageBetweenDate Tests")
    class GetUserAttendancePageBetweenDateTests {

        @Test
        @DisplayName("Should return paginated results correctly")
        void shouldReturnPaginatedResultsCorrectly() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            for (int i = 0; i < 25; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }

            Page<UserAttendanceDTO> page1 = attendanceRepository.getUserAttendancePageBetweenDate(
                    user1Dept1Org1.getId(), startDate, startDate.plus(25, ChronoUnit.DAYS),
                    PageRequest.of(0, 10));

            Page<UserAttendanceDTO> page2 = attendanceRepository.getUserAttendancePageBetweenDate(
                    user1Dept1Org1.getId(), startDate, startDate.plus(25, ChronoUnit.DAYS),
                    PageRequest.of(1, 10));

            assertThat(page1.getTotalElements()).isEqualTo(25);
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(10);
        }

        @Test
        @DisplayName("Should handle empty pages")
        void shouldHandleEmptyPages() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            for (int i = 0; i < 5; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }

            Page<UserAttendanceDTO> emptyPage = attendanceRepository.getUserAttendancePageBetweenDate(
                    user1Dept1Org1.getId(), startDate, startDate.plus(5, ChronoUnit.DAYS),
                    PageRequest.of(10, 5));

            assertThat(emptyPage.hasContent()).isFalse();
            assertThat(emptyPage.getTotalElements()).isEqualTo(5);
            assertThat(emptyPage.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle sorting")
        void shouldHandleSorting() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(2, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.ABSENT);

            Page<UserAttendanceDTO> page = attendanceRepository.getUserAttendancePageBetweenDate(
                    user1Dept1Org1.getId(), startDate, startDate.plus(3, ChronoUnit.DAYS),
                    PageRequest.of(0, 10, Sort.by("date").ascending()));

            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getContent().get(0).getStatus()).isEqualTo(AttendanceStatus.LATE);
            assertThat(page.getContent().get(1).getStatus()).isEqualTo(AttendanceStatus.ABSENT);
            assertThat(page.getContent().get(2).getStatus()).isEqualTo(AttendanceStatus.ON_TIME);
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            Page<UserAttendanceDTO> page = attendanceRepository.getUserAttendancePageBetweenDate(
                    999999L, startDate, endDate, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(0);
            assertThat(page.hasContent()).isFalse();
        }
    }

    @Nested
    @DisplayName("getAttendanceTableRecordsOfUserBetweenDates Tests")
    class GetAttendanceTableRecordsOfUserBetweenDatesTests {

        @Test
        @DisplayName("Should return correct table records")
        void shouldReturnCorrectTableRecords() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            List<Attendance> attendances = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Attendance attendance = createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
                attendances.add(attendance);
            }

            List<UserAttendanceTableRowDTO> records = attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(
                    startDate, startDate.plus(3, ChronoUnit.DAYS), user1Dept1Org1.getId());

            assertThat(records).hasSize(3);
            for (int i = 0; i < records.size(); i++) {
                UserAttendanceTableRowDTO record = records.get(i);
                Attendance expected = attendances.get(i);
                assertThat(record.getId()).isEqualTo(expected.getId());
                assertThat(record.getDate()).isEqualTo(expected.getDate().toEpochMilli());
                assertThat(record.getStatus()).isEqualTo(expected.getStatus());
            }
        }

        @Test
        @DisplayName("Should handle empty results")
        void shouldHandleEmptyResults() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            List<UserAttendanceTableRowDTO> records = attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(
                    startDate, endDate, user1Dept1Org1.getId());

            assertThat(records).isEmpty();
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            List<UserAttendanceTableRowDTO> records = attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(
                    startDate, endDate, 999999L);

            assertThat(records).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAttendanceCountOfUserBetweenDates Tests")
    class GetAttendanceCountOfUserBetweenDatesTests {

        @Test
        @DisplayName("Should accurately count all status types")
        void shouldAccuratelyCountAllStatusTypes() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            int onTimeCount = 5;
            int lateCount = 3;
            int absentCount = 2;
            int onLeaveCount = 1;

            for (int i = 0; i < onTimeCount; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }

            for (int i = 0; i < lateCount; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(onTimeCount + i, ChronoUnit.DAYS), AttendanceStatus.LATE);
            }

            for (int i = 0; i < absentCount; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(onTimeCount + lateCount + i, ChronoUnit.DAYS),
                        AttendanceStatus.ABSENT);
            }

            for (int i = 0; i < onLeaveCount; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(onTimeCount + lateCount + absentCount + i, ChronoUnit.DAYS),
                        AttendanceStatus.ON_LEAVE);
            }

            AttendanceCountDTO count = attendanceRepository.getAttendanceCountOfUserBetweenDates(
                    startDate,
                    startDate.plus(20, ChronoUnit.DAYS),
                    user1Dept1Org1.getId());

            assertThat(count.getDaysPresent()).isEqualTo(onTimeCount + lateCount);
            assertThat(count.getDaysLate()).isEqualTo(lateCount);
            assertThat(count.getDaysAbsent()).isEqualTo(absentCount);
            assertThat(count.getDaysOnLeave()).isEqualTo(onLeaveCount);
        }

        @Test
        @DisplayName("Should return zero counts for user with no attendance")
        void shouldReturnZeroCountsForNoAttendance() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

            AttendanceCountDTO count = attendanceRepository.getAttendanceCountOfUserBetweenDates(
                    startDate,
                    startDate.plus(30, ChronoUnit.DAYS),
                    user1Dept1Org1.getId());

            assertThat(count.getDaysPresent()).isEqualTo(0);
            assertThat(count.getDaysLate()).isEqualTo(0);
            assertThat(count.getDaysAbsent()).isEqualTo(0);
            assertThat(count.getDaysOnLeave()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should use BETWEEN for inclusive date range")
        void shouldUseInclusiveDateRange() {
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);
            Instant day3 = day1.plus(2, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1, day3, AttendanceStatus.ABSENT);

            AttendanceCountDTO count = attendanceRepository.getAttendanceCountOfUserBetweenDates(
                    day1, day3, user1Dept1Org1.getId());

            assertThat(count.getDaysPresent()).isEqualTo(2);
            assertThat(count.getDaysAbsent()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(5, ChronoUnit.DAYS);

            AttendanceCountDTO count = attendanceRepository.getAttendanceCountOfUserBetweenDates(
                    startDate, endDate, 999999L);

            assertThat(count.getDaysPresent()).isEqualTo(0);
            assertThat(count.getDaysLate()).isEqualTo(0);
            assertThat(count.getDaysAbsent()).isEqualTo(0);
            assertThat(count.getDaysOnLeave()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getAllAttendanceIdsOfPresentUsersOfDate Tests")
    class GetAllAttendanceIdsOfPresentUsersOfDateTests {

        @Test
        @DisplayName("Should return IDs of present users only")
        void shouldReturnIdsOfPresentUsersOnly() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            Attendance present1 = createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            Attendance present2 = createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.LATE);
            createAttendanceWithStatus(user3Dept1Org1, date, AttendanceStatus.ABSENT);
            createAttendanceWithStatus(user4Dept1Org1, date, AttendanceStatus.ON_LEAVE);

            List<Long> userIds = Arrays.asList(user1Dept1Org1.getId(), user2Dept1Org1.getId(),
                    user3Dept1Org1.getId(), user4Dept1Org1.getId());

            List<Long> attendanceIds = attendanceRepository.getAllAttendanceIdsOfPresentUsersOfDate(
                    userIds, date);

            assertThat(attendanceIds).hasSize(2);
            assertThat(attendanceIds).containsExactlyInAnyOrder(present1.getId(), present2.getId());
        }

        @Test
        @DisplayName("Should handle empty user list")
        void shouldHandleEmptyUserList() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            List<Long> attendanceIds = attendanceRepository.getAllAttendanceIdsOfPresentUsersOfDate(
                    Collections.emptyList(), date);

            assertThat(attendanceIds).isEmpty();
        }

        @Test
        @DisplayName("Should handle users with no attendance")
        void shouldHandleUsersWithNoAttendance() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);

            List<Long> userIds = Arrays.asList(user1Dept1Org1.getId(), user2Dept1Org1.getId());

            List<Long> attendanceIds = attendanceRepository.getAllAttendanceIdsOfPresentUsersOfDate(
                    userIds, date);

            assertThat(attendanceIds).hasSize(1);
        }

        @Test
        @DisplayName("Should handle null in user list")
        void shouldHandleNullInUserList() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);

            List<Long> userIds = Arrays.asList(user1Dept1Org1.getId(), null);

            List<Long> attendanceIds = attendanceRepository.getAllAttendanceIdsOfPresentUsersOfDate(
                    userIds, date);

            assertThat(attendanceIds).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getDepartmentAttendance Tests")
    class GetDepartmentAttendanceTests {

        @Test
        @DisplayName("Should aggregate attendance by department correctly")
        void shouldAggregateAttendanceByDepartmentCorrectly() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, startDate, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, startDate, AttendanceStatus.LATE);
            createAttendanceWithStatus(user3Dept1Org1, startDate, AttendanceStatus.ABSENT);
            createAttendanceWithStatus(user4Dept1Org1, startDate, AttendanceStatus.ON_LEAVE);

            createAttendanceWithStatus(user1Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, startDate.plus(1, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);

            Optional<DepartmentAttendanceStatsDTO> result = attendanceRepository.getDepartmentAttendance(
                    dept1Org1.getId(), startDate, endDate);

            assertThat(result.isPresent()).isTrue();
            assertThat(result.get().getId()).isEqualTo(dept1Org1.getId());
            assertThat(result.get().getDepartmentName()).isEqualTo(dept1Org1.getDepartmentName());
            assertThat(result.get().getPresent()).isEqualTo(4);
            assertThat(result.get().getLate()).isEqualTo(1);
            assertThat(result.get().getAbsent()).isEqualTo(1);
            assertThat(result.get().getOnLeave()).isEqualTo(1);
            assertThat(result.get().getOnTime()).isEqualTo(3);
            assertThat(result.get().getTotal()).isEqualTo(6);
        }

        @Test
        @DisplayName("Should handle department with no attendance")
        void shouldHandleDepartmentWithNoAttendance() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            Optional<DepartmentAttendanceStatsDTO> result = attendanceRepository.getDepartmentAttendance(
                    dept1Org1.getId(), startDate, endDate);

            assertThat(result.isPresent()).isFalse();
        }

        @Test
        @DisplayName("Should handle invalid department ID")
        void shouldHandleInvalidDepartmentId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            Optional<DepartmentAttendanceStatsDTO> result = attendanceRepository.getDepartmentAttendance(
                    999999L, startDate, endDate);

            assertThat(result.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("getOrganizationAttendanceChartInfo Tests")
    class GetOrganizationAttendanceChartInfoTests {

        @Test
        @DisplayName("Should aggregate daily attendance for organization")
        void shouldAggregateDailyAttendanceForOrganization() {
            Instant day1 = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant day2 = day1.plus(1, ChronoUnit.DAYS);
            Instant day3 = day1.plus(2, ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, day1, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, day1, AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept2Org1, day1, AttendanceStatus.ABSENT);

            createAttendanceWithStatus(user1Dept1Org1, day2, AttendanceStatus.ON_LEAVE);
            createAttendanceWithStatus(user1Dept2Org1, day2, AttendanceStatus.ON_TIME);

            List<DailyAttendanceGraphDataDTO> results =
                    attendanceRepository.getOrganizationAttendanceChartInfo(org1.getId(), day1, day3);

            assertThat(results).hasSize(2);

            DailyAttendanceGraphDataDTO day1Data = results.stream()
                    .filter(r -> r.getDate().equals(day1.toEpochMilli()))
                    .findFirst().orElseThrow();

            assertThat(day1Data.getPresentCount()).isEqualTo(2);
            assertThat(day1Data.getLateCount()).isEqualTo(1);
            assertThat(day1Data.getAbsentCount()).isEqualTo(1);
            assertThat(day1Data.getLeaveCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle organization with no attendance")
        void shouldHandleOrganizationWithNoAttendance() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            List<DailyAttendanceGraphDataDTO> results =
                    attendanceRepository.getOrganizationAttendanceChartInfo(org1.getId(), startDate, endDate);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle invalid organization ID")
        void shouldHandleInvalidOrganizationId() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            List<DailyAttendanceGraphDataDTO> results =
                    attendanceRepository.getOrganizationAttendanceChartInfo(999999L, startDate, endDate);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserAttendanceGraphData Tests")
    class GetUserAttendanceGraphDataTests {

        @Test
        @DisplayName("Should aggregate monthly attendance for user")
        void shouldAggregateMonthlyAttendanceForUser() {
            Instant startDate = LocalDate.of(2024, 1, 1)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant endDate = LocalDate.of(2024, 3, 31)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();

            for (int i = 0; i < 5; i++) {
                createAttendanceWithStatus(user1Dept1Org1,
                        startDate.plus(i, ChronoUnit.DAYS), AttendanceStatus.ON_TIME);
            }
            createAttendanceWithStatus(user1Dept1Org1,
                    startDate.plus(5, ChronoUnit.DAYS), AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1,
                    startDate.plus(6, ChronoUnit.DAYS), AttendanceStatus.LATE);
            createAttendanceWithStatus(user1Dept1Org1,
                    startDate.plus(7, ChronoUnit.DAYS), AttendanceStatus.ABSENT);
            createAttendanceWithStatus(user1Dept1Org1,
                    startDate.plus(8, ChronoUnit.DAYS), AttendanceStatus.ON_LEAVE);

            Optional<MonthlyAttendanceGraphDataDTO> result =
                    attendanceRepository.getUserAttendanceGraphData(user1Dept1Org1.getId(), startDate, endDate);

            assertThat(result).isPresent();
            assertThat(result.get().getPresentCount()).isEqualTo(7);
            assertThat(result.get().getLateCount()).isEqualTo(2);
            assertThat(result.get().getAbsentCount()).isEqualTo(1);
            assertThat(result.get().getLeaveCount()).isEqualTo(1);
            assertThat(result.get().getMonth()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle user with no attendance")
        void shouldHandleUserWithNoAttendance() {
            Instant startDate = LocalDate.of(2024, 1, 1)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant endDate = LocalDate.of(2024, 1, 31)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();

            Optional<MonthlyAttendanceGraphDataDTO> result =
                    attendanceRepository.getUserAttendanceGraphData(user1Dept1Org1.getId(), startDate, endDate);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            Instant startDate = LocalDate.of(2024, 1, 1)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant endDate = LocalDate.of(2024, 1, 31)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();

            Optional<MonthlyAttendanceGraphDataDTO> result =
                    attendanceRepository.getUserAttendanceGraphData(999999L, startDate, endDate);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserIdsOfLeaveOfDate Tests")
    class GetUserIdsOfLeaveOfDateTests {

        @Test
        @DisplayName("Should return user IDs on leave for specific date")
        void shouldReturnUserIdsOnLeaveForSpecificDate() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_LEAVE);
            createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.ON_LEAVE);
            createAttendanceWithStatus(user3Dept1Org1, date, AttendanceStatus.ON_TIME);

            List<Long> userIds = attendanceRepository.getUserIdsOfLeaveOfDate(date);

            assertThat(userIds).hasSize(2);
            assertThat(userIds).containsExactlyInAnyOrder(user1Dept1Org1.getId(), user2Dept1Org1.getId());
        }

        @Test
        @DisplayName("Should return empty list for date with no leave")
        void shouldReturnEmptyListForDateWithNoLeave() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.ABSENT);

            List<Long> userIds = attendanceRepository.getUserIdsOfLeaveOfDate(date);

            assertThat(userIds).isEmpty();
        }

        @Test
        @DisplayName("Should handle date with no attendance")
        void shouldHandleDateWithNoAttendance() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            List<Long> userIds = attendanceRepository.getUserIdsOfLeaveOfDate(date);

            assertThat(userIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrganizationAttendanceStatisticsForDate Tests")
    class GetOrganizationAttendanceStatisticsForDateTests {

        @Test
        @DisplayName("Should calculate accurate statistics for organization")
        void shouldCalculateAccurateStatisticsForOrganization() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            createAttendanceWithStatus(user1Dept1Org1, date, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user2Dept1Org1, date, AttendanceStatus.ON_TIME);
            createAttendanceWithStatus(user3Dept1Org1, date, AttendanceStatus.LATE);
            createAttendanceWithStatus(user4Dept1Org1, date, AttendanceStatus.ABSENT);
            createAttendanceWithStatus(user1Dept2Org1, date, AttendanceStatus.ON_LEAVE);

            OrganizationAttendanceStatsDTO stats =
                    attendanceRepository.getOrganizationAttendanceStatisticsForDate(org1.getId(), date);

            assertThat(stats.getTotalUsers()).isEqualTo(5L);
            assertThat(stats.getUsersPresent()).isEqualTo(3L);
            assertThat(stats.getUsersOnTime()).isEqualTo(2L);
            assertThat(stats.getUsersLate()).isEqualTo(1L);
            assertThat(stats.getUsersAbsent()).isEqualTo(1L);
            assertThat(stats.getUsersOnLeave()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return zero statistics for date with no attendance")
        void shouldReturnZeroStatisticsForDateWithNoAttendance() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            OrganizationAttendanceStatsDTO stats =
                    attendanceRepository.getOrganizationAttendanceStatisticsForDate(org1.getId(), date);

            assertThat(stats.getTotalUsers()).isEqualTo(0L);
            assertThat(stats.getUsersPresent()).isEqualTo(0L);
            assertThat(stats.getUsersOnTime()).isEqualTo(0L);
            assertThat(stats.getUsersLate()).isEqualTo(0L);
            assertThat(stats.getUsersAbsent()).isEqualTo(0L);
            assertThat(stats.getUsersOnLeave()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle invalid organization ID")
        void shouldHandleInvalidOrganizationId() {
            Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS);

            OrganizationAttendanceStatsDTO stats =
                    attendanceRepository.getOrganizationAttendanceStatisticsForDate(999999L, date);

            assertThat(stats.getTotalUsers()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getUsersAttendanceExcelData Tests")
    class GetUsersAttendanceExcelDataTests {

        @Test
        @DisplayName("Should get attendance excel data for multiple users")
        void shouldGetAttendanceExcelDataForMultipleUsers() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, startDate, 3);
            createAttendanceRecords(user2Dept1Org1, startDate, 2);

            List<AttendanceExcelDataDTO> result = attendanceRepository.getUsersAttendanceExcelData(
                    startDate, endDate, Arrays.asList(user1Dept1Org1.getId(), user2Dept1Org1.getId()));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(AttendanceExcelDataDTO::getUserId)
                    .containsExactlyInAnyOrder(user1Dept1Org1.getId(), user2Dept1Org1.getId());
        }

        @Test
        @DisplayName("Should return empty list when no attendance in date range")
        void shouldReturnEmptyListWhenNoAttendanceInDateRange() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);
            Instant outsideDate = startDate.minus(10, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, outsideDate, 3);

            List<AttendanceExcelDataDTO> result = attendanceRepository.getUsersAttendanceExcelData(
                    startDate, endDate, Collections.singletonList(user1Dept1Org1.getId()));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty user list")
        void shouldHandleEmptyUserList() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            List<AttendanceExcelDataDTO> result = attendanceRepository.getUsersAttendanceExcelData(
                    startDate, endDate, Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle users with no attendance")
        void shouldHandleUsersWithNoAttendance() {
            Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(3, ChronoUnit.DAYS);

            createAttendanceRecords(user1Dept1Org1, startDate, 2);

            List<AttendanceExcelDataDTO> result = attendanceRepository.getUsersAttendanceExcelData(
                    startDate, endDate, Arrays.asList(user1Dept1Org1.getId(), user2Dept1Org1.getId()));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUserId()).isEqualTo(user1Dept1Org1.getId());
        }
    }
}