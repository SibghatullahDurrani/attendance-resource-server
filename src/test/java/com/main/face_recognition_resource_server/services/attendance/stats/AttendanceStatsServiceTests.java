package com.main.face_recognition_resource_server.services.attendance.stats;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCountDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.DepartmentAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.OrganizationAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceStatsDTO;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.checkin.CheckInService;
import com.main.face_recognition_resource_server.services.checkout.CheckOutService;
import com.main.face_recognition_resource_server.services.department.DepartmentService;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceStatsService Unit Tests")
class AttendanceStatsServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private CheckInService checkInService;

    @Mock
    private CheckOutService checkOutService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private AttendanceStatsServiceImpl attendanceStatsService;

    private static final Long USER_ID = 1L;
    private static final Long ORGANIZATION_ID = 100L;
    private static final String TIME_ZONE = "America/New_York";
    private static final int YEAR = 2024;
    private static final int MONTH = 3;
    private static final int DAY = 15;

    @BeforeEach
    void setUp() {
        reset(userService, attendanceRepository, checkInService, checkOutService,
               organizationService, departmentService);
    }

    @Nested
    @DisplayName("getUserAttendanceStats(year, userId) Tests")
    class GetUserAttendanceStatsByYear {

        @Test
        @DisplayName("Should return stats for valid year and user with attendance")
        void shouldReturnStatsForValidYearAndUser() throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(200)
                .daysAbsent(50)
                .daysOnLeave(10)
                .daysLate(5)
                .build();

            List<Long> attendanceIds = Arrays.asList(1L, 2L, 3L);
            String avgCheckIn = "09:00 AM";
            String avgCheckOut = "06:00 PM";

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceIds);
                when(checkInService.getAverageCheckInOfAttendances(attendanceIds, TIME_ZONE))
                    .thenReturn(avgCheckIn);
                when(checkOutService.getAverageCheckOutOfAttendances(attendanceIds, TIME_ZONE))
                    .thenReturn(avgCheckOut);

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAttendanceCount()).isEqualTo(attendanceCount);
                assertThat(result.getAverageCheckInTime()).isEqualTo(avgCheckIn);
                assertThat(result.getAverageCheckOutTime()).isEqualTo(avgCheckOut);

                verify(userService, times(2)).getUserTimeZone(USER_ID);
                verify(attendanceRepository).getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID);
                verify(checkInService).getAverageCheckInOfAttendances(attendanceIds, TIME_ZONE);
                verify(checkOutService).getAverageCheckOutOfAttendances(attendanceIds, TIME_ZONE);
            }
        }

        @Test
        @DisplayName("Should handle NoStatsAvailableException when no check-ins but has absences")
        void shouldHandleNoCheckInsButHasAbsences() throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(0)
                .daysAbsent(365)
                .daysOnLeave(0)
                .daysLate(0)
                .build();

            List<Long> attendanceIds = Collections.emptyList();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceIds);
                when(checkInService.getAverageCheckInOfAttendances(attendanceIds, TIME_ZONE))
                    .thenThrow(new NoStatsAvailableException());

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAttendanceCount()).isEqualTo(attendanceCount);
                assertThat(result.getAverageCheckInTime()).isEqualTo("-");
                assertThat(result.getAverageCheckOutTime()).isEqualTo("-");
            }
        }

        @Test
        @DisplayName("Should throw NoStatsAvailableException when no attendance data at all")
        void shouldThrowExceptionWhenNoAttendanceData() throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(0)
                .daysAbsent(0)
                .daysOnLeave(0)
                .daysLate(0)
                .build();

            List<Long> attendanceIds = Collections.emptyList();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceIds);
                when(checkInService.getAverageCheckInOfAttendances(attendanceIds, TIME_ZONE))
                    .thenThrow(new NoStatsAvailableException());

                // Act & Assert
                assertThatThrownBy(() -> attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID))
                    .isInstanceOf(NoStatsAvailableException.class);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {2020, 2021, 2022, 2023, 2024, 2025})
        @DisplayName("Should handle different years correctly")
        void shouldHandleDifferentYears(int year) throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(100)
                .daysAbsent(50)
                .daysOnLeave(10)
                .daysLate(5)
                .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse(year + "-01-01T00:00:00Z");
                Instant endDate = Instant.parse(year + "-12-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(year, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(any(), any(), eq(USER_ID)))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(any(), any(), eq(USER_ID)))
                    .thenReturn(Arrays.asList(1L, 2L));
                when(checkInService.getAverageCheckInOfAttendances(any(), eq(TIME_ZONE)))
                    .thenReturn("09:00 AM");
                when(checkOutService.getAverageCheckOutOfAttendances(any(), eq(TIME_ZONE)))
                    .thenReturn("06:00 PM");

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(year, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAttendanceCount().getDaysPresent()).isEqualTo(100);
            }
        }
    }

    @Nested
    @DisplayName("getUserAttendanceStats(month, year, userId) Tests")
    class GetUserAttendanceStatsByMonthYear {

        @Test
        @DisplayName("Should return stats for valid month, year and user")
        void shouldReturnStatsForValidMonthYearAndUser() throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(20)
                .daysAbsent(5)
                .daysOnLeave(2)
                .daysLate(1)
                .build();

            List<Long> attendanceIds = Arrays.asList(1L, 2L, 3L);
            String avgCheckIn = "09:15 AM";
            String avgCheckOut = "06:30 PM";

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceIds);
                when(checkInService.getAverageCheckInOfAttendances(attendanceIds, TIME_ZONE))
                    .thenReturn(avgCheckIn);
                when(checkOutService.getAverageCheckOutOfAttendances(attendanceIds, TIME_ZONE))
                    .thenReturn(avgCheckOut);

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(MONTH, YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAttendanceCount()).isEqualTo(attendanceCount);
                assertThat(result.getAverageCheckInTime()).isEqualTo(avgCheckIn);
                assertThat(result.getAverageCheckOutTime()).isEqualTo(avgCheckOut);
            }
        }

        @ParameterizedTest
        @CsvSource({
            "1, 2024",  // January
            "2, 2024",  // February (leap year)
            "2, 2023",  // February (non-leap year)
            "4, 2024",  // April (30 days)
            "12, 2024"  // December
        })
        @DisplayName("Should handle different months correctly")
        void shouldHandleDifferentMonths(int month, int year) throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(15)
                .daysAbsent(10)
                .daysOnLeave(3)
                .daysLate(2)
                .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(any(), any(), eq(USER_ID)))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(any(), any(), eq(USER_ID)))
                    .thenReturn(Arrays.asList(1L));
                when(checkInService.getAverageCheckInOfAttendances(any(), eq(TIME_ZONE)))
                    .thenReturn("09:00 AM");
                when(checkOutService.getAverageCheckOutOfAttendances(any(), eq(TIME_ZONE)))
                    .thenReturn("06:00 PM");

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(month, year, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAttendanceCount().getDaysPresent()).isEqualTo(15);
            }
        }

        @Test
        @DisplayName("Should handle edge case with no attendance but absences in month")
        void shouldHandleNoAttendanceWithAbsencesInMonth() throws NoStatsAvailableException {
            // Arrange
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(0)
                .daysAbsent(30)
                .daysOnLeave(0)
                .daysLate(0)
                .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(Collections.emptyList());
                when(checkInService.getAverageCheckInOfAttendances(any(), eq(TIME_ZONE)))
                    .thenThrow(new NoStatsAvailableException());

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(MONTH, YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAverageCheckInTime()).isEqualTo("-");
                assertThat(result.getAverageCheckOutTime()).isEqualTo("-");
                assertThat(result.getAttendanceCount().getDaysAbsent()).isEqualTo(30);
            }
        }
    }

    @Nested
    @DisplayName("getCurrentDayOrganizationAttendanceStatistics Tests")
    class GetCurrentDayOrganizationStats {

        @Test
        @DisplayName("Should return organization stats for current day")
        void shouldReturnOrganizationStatsForCurrentDay() {
            // Arrange
            OrganizationAttendanceStatsDTO expectedStats = new OrganizationAttendanceStatsDTO();
            // Assuming the DTO has some fields, set them as needed

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant today = Instant.parse("2024-03-15T00:00:00Z");
                mockedDateUtils.when(() -> DateUtils.getStartDateOfToday(TIME_ZONE))
                    .thenReturn(today);

                when(attendanceRepository.getOrganizationAttendanceStatisticsForDate(ORGANIZATION_ID, today))
                    .thenReturn(expectedStats);

                // Act
                OrganizationAttendanceStatsDTO result =
                    attendanceStatsService.getCurrentDayOrganizationAttendanceStatistics(ORGANIZATION_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).isEqualTo(expectedStats);

                verify(organizationService).getOrganizationTimeZone(ORGANIZATION_ID);
                verify(attendanceRepository).getOrganizationAttendanceStatisticsForDate(ORGANIZATION_ID, today);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney", "UTC"})
        @DisplayName("Should handle different time zones correctly")
        void shouldHandleDifferentTimeZones(String timeZone) {
            // Arrange
            OrganizationAttendanceStatsDTO expectedStats = new OrganizationAttendanceStatsDTO();

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(timeZone);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant today = Instant.now();
                mockedDateUtils.when(() -> DateUtils.getStartDateOfToday(timeZone))
                    .thenReturn(today);

                when(attendanceRepository.getOrganizationAttendanceStatisticsForDate(ORGANIZATION_ID, today))
                    .thenReturn(expectedStats);

                // Act
                OrganizationAttendanceStatsDTO result =
                    attendanceStatsService.getCurrentDayOrganizationAttendanceStatistics(ORGANIZATION_ID);

                // Assert
                assertThat(result).isNotNull();
                verify(organizationService).getOrganizationTimeZone(ORGANIZATION_ID);
            }
        }

        @Test
        @DisplayName("Should handle null organization stats gracefully")
        void shouldHandleNullOrganizationStats() {
            // Arrange
            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant today = Instant.parse("2024-03-15T00:00:00Z");
                mockedDateUtils.when(() -> DateUtils.getStartDateOfToday(TIME_ZONE))
                    .thenReturn(today);

                when(attendanceRepository.getOrganizationAttendanceStatisticsForDate(ORGANIZATION_ID, today))
                    .thenReturn(null);

                // Act
                OrganizationAttendanceStatsDTO result =
                    attendanceStatsService.getCurrentDayOrganizationAttendanceStatistics(ORGANIZATION_ID);

                // Assert
                assertThat(result).isNull();
            }
        }
    }

    @Nested
    @DisplayName("getOrganizationDepartmentsAttendanceStats Tests")
    class GetOrganizationDepartmentsStats {

        @Test
        @DisplayName("Should return department stats for all departments in organization")
        void shouldReturnDepartmentStatsForAllDepartments() {
            // Arrange
            List<Long> departmentIds = Arrays.asList(1L, 2L, 3L);
            DepartmentAttendanceStatsDTO dept1Stats = new DepartmentAttendanceStatsDTO();
            DepartmentAttendanceStatsDTO dept2Stats = new DepartmentAttendanceStatsDTO();

            when(departmentService.getDepartmentIdsOfOrganization(ORGANIZATION_ID))
                .thenReturn(departmentIds);
            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID))
                .thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-15T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-15T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(DAY, MONTH, YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getDepartmentAttendance(1L, startDate, endDate))
                    .thenReturn(Optional.of(dept1Stats));
                when(attendanceRepository.getDepartmentAttendance(2L, startDate, endDate))
                    .thenReturn(Optional.of(dept2Stats));
                when(attendanceRepository.getDepartmentAttendance(3L, startDate, endDate))
                    .thenReturn(Optional.empty());

                // Act
                List<DepartmentAttendanceStatsDTO> result =
                    attendanceStatsService.getOrganizationDepartmentsAttendanceStats(ORGANIZATION_ID, YEAR, MONTH, DAY);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);
                assertThat(result).containsExactly(dept1Stats, dept2Stats);

                verify(departmentService).getDepartmentIdsOfOrganization(ORGANIZATION_ID);
                verify(attendanceRepository, times(3)).getDepartmentAttendance(anyLong(), eq(startDate), eq(endDate));
            }
        }

        @Test
        @DisplayName("Should return empty list when no departments exist")
        void shouldReturnEmptyListWhenNoDepartments() {
            // Arrange
            when(departmentService.getDepartmentIdsOfOrganization(ORGANIZATION_ID))
                .thenReturn(Collections.emptyList());
            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID))
                .thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-15T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-15T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(DAY, MONTH, YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                // Act
                List<DepartmentAttendanceStatsDTO> result =
                    attendanceStatsService.getOrganizationDepartmentsAttendanceStats(ORGANIZATION_ID, YEAR, MONTH, DAY);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).isEmpty();

                verify(attendanceRepository, never()).getDepartmentAttendance(anyLong(), any(), any());
            }
        }

        @Test
        @DisplayName("Should handle when all departments have no attendance data")
        void shouldHandleWhenAllDepartmentsHaveNoData() {
            // Arrange
            List<Long> departmentIds = Arrays.asList(1L, 2L, 3L);

            when(departmentService.getDepartmentIdsOfOrganization(ORGANIZATION_ID))
                .thenReturn(departmentIds);
            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID))
                .thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-15T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-15T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(DAY, MONTH, YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getDepartmentAttendance(anyLong(), eq(startDate), eq(endDate)))
                    .thenReturn(Optional.empty());

                // Act
                List<DepartmentAttendanceStatsDTO> result =
                    attendanceStatsService.getOrganizationDepartmentsAttendanceStats(ORGANIZATION_ID, YEAR, MONTH, DAY);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).isEmpty();

                verify(attendanceRepository, times(3)).getDepartmentAttendance(anyLong(), eq(startDate), eq(endDate));
            }
        }

        @ParameterizedTest
        @CsvSource({
            "1, 1, 2024",   // January 1st
            "31, 12, 2024", // December 31st
            "29, 2, 2024",  // Leap year Feb 29th
            "15, 6, 2024"   // Mid-year date
        })
        @DisplayName("Should handle various dates correctly")
        void shouldHandleVariousDates(int day, int month, int year) {
            // Arrange
            List<Long> departmentIds = Arrays.asList(1L);
            DepartmentAttendanceStatsDTO deptStats = new DepartmentAttendanceStatsDTO();

            when(departmentService.getDepartmentIdsOfOrganization(ORGANIZATION_ID))
                .thenReturn(departmentIds);
            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID))
                .thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(day, month, year, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getDepartmentAttendance(1L, startDate, endDate))
                    .thenReturn(Optional.of(deptStats));

                // Act
                List<DepartmentAttendanceStatsDTO> result =
                    attendanceStatsService.getOrganizationDepartmentsAttendanceStats(ORGANIZATION_ID, year, month, day);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(deptStats);
            }
        }

        @Test
        @DisplayName("Should handle mixed results - some departments with data, some without")
        void shouldHandleMixedResults() {
            // Arrange
            List<Long> departmentIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
            DepartmentAttendanceStatsDTO dept1Stats = new DepartmentAttendanceStatsDTO();
            DepartmentAttendanceStatsDTO dept3Stats = new DepartmentAttendanceStatsDTO();
            DepartmentAttendanceStatsDTO dept5Stats = new DepartmentAttendanceStatsDTO();

            when(departmentService.getDepartmentIdsOfOrganization(ORGANIZATION_ID))
                .thenReturn(departmentIds);
            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID))
                .thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-15T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-15T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(DAY, MONTH, YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getDepartmentAttendance(1L, startDate, endDate))
                    .thenReturn(Optional.of(dept1Stats));
                when(attendanceRepository.getDepartmentAttendance(2L, startDate, endDate))
                    .thenReturn(Optional.empty());
                when(attendanceRepository.getDepartmentAttendance(3L, startDate, endDate))
                    .thenReturn(Optional.of(dept3Stats));
                when(attendanceRepository.getDepartmentAttendance(4L, startDate, endDate))
                    .thenReturn(Optional.empty());
                when(attendanceRepository.getDepartmentAttendance(5L, startDate, endDate))
                    .thenReturn(Optional.of(dept5Stats));

                // Act
                List<DepartmentAttendanceStatsDTO> result =
                    attendanceStatsService.getOrganizationDepartmentsAttendanceStats(ORGANIZATION_ID, YEAR, MONTH, DAY);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(3);
                assertThat(result).containsExactly(dept1Stats, dept3Stats, dept5Stats);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrors {

        @Test
        @DisplayName("Should handle null time zone gracefully")
        void shouldHandleNullTimeZone() throws NoStatsAvailableException {
            // This test verifies the service doesn't crash with null timezone
            // In production, this might throw an exception from DateUtils

            when(userService.getUserTimeZone(USER_ID)).thenReturn(null);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(YEAR, null))
                    .thenThrow(new NullPointerException("Time zone cannot be null"));

                // Act & Assert
                assertThatThrownBy(() -> attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Time zone cannot be null");
            }
        }

        @Test
        @DisplayName("Should handle very large attendance ID lists")
        void shouldHandleVeryLargeAttendanceIdLists() throws NoStatsAvailableException {
            // Arrange - Create a large list of attendance IDs
            List<Long> largeAttendanceIds = new ArrayList<>();
            for (long i = 1; i <= 10000; i++) {
                largeAttendanceIds.add(i);
            }

            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(10000)
                .daysAbsent(0)
                .daysOnLeave(0)
                .daysLate(500)
                .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(largeAttendanceIds);
                when(checkInService.getAverageCheckInOfAttendances(largeAttendanceIds, TIME_ZONE))
                    .thenReturn("09:00 AM");
                when(checkOutService.getAverageCheckOutOfAttendances(largeAttendanceIds, TIME_ZONE))
                    .thenReturn("06:00 PM");

                // Act
                UserAttendanceStatsDTO result = attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAttendanceCount().getDaysPresent()).isEqualTo(10000);
            }
        }

        @Test
        @DisplayName("Should handle concurrent calls for same user")
        void shouldHandleConcurrentCallsForSameUser() throws NoStatsAvailableException {
            // This test simulates concurrent calls which should not interfere with each other
            AttendanceCountDTO attendanceCount = AttendanceCountDTO.builder()
                .daysPresent(100)
                .daysAbsent(50)
                .daysOnLeave(10)
                .daysLate(5)
                .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfYearInTimeZone(YEAR, TIME_ZONE))
                    .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(attendanceCount);
                when(attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, USER_ID))
                    .thenReturn(Arrays.asList(1L, 2L));
                when(checkInService.getAverageCheckInOfAttendances(any(), eq(TIME_ZONE)))
                    .thenReturn("09:00 AM");
                when(checkOutService.getAverageCheckOutOfAttendances(any(), eq(TIME_ZONE)))
                    .thenReturn("06:00 PM");

                // Act - Simulate multiple calls
                UserAttendanceStatsDTO result1 = attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID);
                UserAttendanceStatsDTO result2 = attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID);
                UserAttendanceStatsDTO result3 = attendanceStatsService.getUserAttendanceStats(YEAR, USER_ID);

                // Assert
                assertThat(result1).isNotNull();
                assertThat(result2).isNotNull();
                assertThat(result3).isNotNull();
                assertThat(result1.getAttendanceCount()).isEqualTo(result2.getAttendanceCount());
                assertThat(result2.getAttendanceCount()).isEqualTo(result3.getAttendanceCount());

                // Verify the mocks were called the expected number of times
                verify(userService, times(6)).getUserTimeZone(USER_ID); // 2 times per call
                verify(attendanceRepository, times(3)).getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }
    }
}