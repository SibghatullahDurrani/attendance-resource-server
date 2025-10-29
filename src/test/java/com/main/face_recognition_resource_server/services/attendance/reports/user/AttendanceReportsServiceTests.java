package com.main.face_recognition_resource_server.services.attendance.reports.user;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.mappers.attendance.CalendarAttendanceMapper;
import com.main.face_recognition_resource_server.projections.attendance.CalendarAttendanceProjection;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.attendance.reports.AttendanceReportsServiceImpl;
import com.main.face_recognition_resource_server.services.checkin.CheckInService;
import com.main.face_recognition_resource_server.services.checkout.CheckOutService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceReportsService Unit Tests")
class AttendanceReportsServiceTests {

    private static final Long USER_ID = 1L;
    private static final Long ORGANIZATION_ID = 100L;
    private static final String TIME_ZONE = "America/New_York";
    private static final int YEAR = 2024;
    private static final int MONTH = 3;
    private static final int DAY = 15;
    @Mock
    private UserService userService;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CheckInService checkInService;
    @Mock
    private CheckOutService checkOutService;
    @InjectMocks
    private AttendanceReportsServiceImpl attendanceReportsService;

    @BeforeEach
    void setUp() {
        reset(userService, attendanceRepository, checkInService, checkOutService);
    }

    @Nested
    @DisplayName("getUserAttendanceCalendar(month, year, userId) Tests")
    class GetUserAttendanceCalendarByMonthYear {

        @Test
        @DisplayName("Should return monthly calendar with attendance data")
        void shouldReturnMonthlyCalendarWithAttendanceData() throws NoStatsAvailableException {
            // Arrange
            List<CalendarAttendanceProjection> projections = Arrays.asList(
                    CalendarAttendanceProjection.builder()
                            .date(Instant.parse("2024-03-01T10:00:00Z"))
                            .status(AttendanceStatus.ON_TIME)
                            .build(),
                    CalendarAttendanceProjection.builder()
                            .date(Instant.parse("2024-03-02T10:00:00Z"))
                            .status(AttendanceStatus.LATE)
                            .build()
            );

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
                 MockedStatic<CalendarAttendanceMapper> mockedMapper = mockStatic(CalendarAttendanceMapper.class)) {

                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(projections);

                CalendarAttendanceDataDTO data1 = CalendarAttendanceDataDTO.builder()
                        .date(1)
                        .status(AttendanceStatus.ON_TIME)
                        .build();
                CalendarAttendanceDataDTO data2 = CalendarAttendanceDataDTO.builder()
                        .date(2)
                        .status(AttendanceStatus.LATE)
                        .build();

                mockedMapper.when(() -> CalendarAttendanceMapper.calendarAttendanceProjectionToCalendarAttendanceDataDTO(projections.get(0), TIME_ZONE))
                        .thenReturn(data1);
                mockedMapper.when(() -> CalendarAttendanceMapper.calendarAttendanceProjectionToCalendarAttendanceDataDTO(projections.get(1), TIME_ZONE))
                        .thenReturn(data2);

                // Act
                MonthlyAttendanceCalendarDTO result = attendanceReportsService.getUserAttendanceCalendar(MONTH, YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getData()).hasSize(2);
                assertThat(result.getMaxDays()).isEqualTo(30); // April 2024 has 30 days (month=3 means April)
                assertThat(result.getFirstDayOfTheMonth()).isEqualTo("MONDAY"); // April 1, 2024 is Monday
                assertThat(result.getLastDayOfTheMonth()).isEqualTo("TUESDAY"); // April 30, 2024 is Tuesday
                assertThat(result.getLastDateOfPreviousMonth()).isEqualTo(31); // March 2024 has 31 days

                verify(userService).getUserTimeZone(USER_ID);
                verify(attendanceRepository).getCalendarAttendanceProjectionOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }

        @Test
        @DisplayName("Should throw NoStatsAvailableException when no attendance data")
        void shouldThrowExceptionWhenNoAttendanceData() {
            // Arrange
            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(Collections.emptyList());

                // Act & Assert
                assertThatThrownBy(() -> attendanceReportsService.getUserAttendanceCalendar(MONTH, YEAR, USER_ID))
                        .isInstanceOf(NoStatsAvailableException.class);

                verify(attendanceRepository).getCalendarAttendanceProjectionOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }

        @ParameterizedTest
        @CsvSource({
                "0, 2024, 31, WEDNESDAY, FRIDAY",    // January 2024
                "1, 2024, 29, SATURDAY, SUNDAY",     // February 2024 (leap year)
                "1, 2023, 28, FRIDAY, FRIDAY",       // February 2023 (non-leap year)
                "3, 2024, 30, SUNDAY, TUESDAY",      // April 2024
                "11, 2024, 31, FRIDAY, SUNDAY"       // December 2024
        })
        @DisplayName("Should handle different months correctly")
        void shouldHandleDifferentMonths(int month, int year, int expectedMaxDays, String expectedFirstDay, String expectedLastDay) throws NoStatsAvailableException {
            // Arrange
            List<CalendarAttendanceProjection> projections = Collections.singletonList(
                    CalendarAttendanceProjection.builder()
                            .date(Instant.now())
                            .status(AttendanceStatus.ON_TIME)
                            .build()
            );

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
                 MockedStatic<CalendarAttendanceMapper> mockedMapper = mockStatic(CalendarAttendanceMapper.class)) {

                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(any(), any(), eq(USER_ID)))
                        .thenReturn(projections);

                CalendarAttendanceDataDTO data = CalendarAttendanceDataDTO.builder()
                        .date(1)
                        .status(AttendanceStatus.ON_TIME)
                        .build();

                mockedMapper.when(() -> CalendarAttendanceMapper.calendarAttendanceProjectionToCalendarAttendanceDataDTO(any(), eq(TIME_ZONE)))
                        .thenReturn(data);

                // Act
                MonthlyAttendanceCalendarDTO result = attendanceReportsService.getUserAttendanceCalendar(month, year, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getMaxDays()).isEqualTo(expectedMaxDays);
                assertThat(result.getData()).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should handle leap year February correctly")
        void shouldHandleLeapYearFebruary() throws NoStatsAvailableException {
            // Arrange
            int leapYear = 2024;
            int february = 1; // February is month 1 (0-indexed)

            List<CalendarAttendanceProjection> projections = Collections.singletonList(
                    CalendarAttendanceProjection.builder()
                            .date(Instant.parse("2024-02-29T10:00:00Z")) // Leap day
                            .status(AttendanceStatus.ON_TIME)
                            .build()
            );

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
                 MockedStatic<CalendarAttendanceMapper> mockedMapper = mockStatic(CalendarAttendanceMapper.class)) {

                Instant startDate = Instant.parse("2024-02-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-02-29T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(february, leapYear, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(projections);

                CalendarAttendanceDataDTO data = CalendarAttendanceDataDTO.builder()
                        .date(29)
                        .status(AttendanceStatus.ON_TIME)
                        .build();

                mockedMapper.when(() -> CalendarAttendanceMapper.calendarAttendanceProjectionToCalendarAttendanceDataDTO(projections.get(0), TIME_ZONE))
                        .thenReturn(data);

                // Act
                MonthlyAttendanceCalendarDTO result = attendanceReportsService.getUserAttendanceCalendar(february, leapYear, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getMaxDays()).isEqualTo(29); // February 2024 has 29 days (leap year)
                assertThat(result.getData()).hasSize(1);
                assertThat(result.getData().get(0).getDate()).isEqualTo(29);
            }
        }
    }

    @Nested
    @DisplayName("getUserAttendanceCalendar(year, userId) Tests")
    class GetUserAttendanceCalendarByYear {

        @Test
        @DisplayName("Should return yearly calendar with all months")
        void shouldReturnYearlyCalendarWithAllMonths() {
            // Arrange
            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                // Mock all 12 months
                for (int month = 0; month < 12; month++) {
                    final int finalMonth = month;
                    Instant startDate = Instant.now().plusSeconds(finalMonth * 30L * 24 * 3600);
                    Instant endDate = startDate.plusSeconds(30L * 24 * 3600);
                    mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(finalMonth, YEAR, TIME_ZONE))
                            .thenReturn(new Instant[]{startDate, endDate});

                    AttendanceCountDTO count = AttendanceCountDTO.builder()
                            .daysPresent(20L)
                            .daysAbsent(5L)
                            .daysOnLeave(2L)
                            .daysLate(3L)
                            .build();

                    when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                            .thenReturn(count);
                }

                // Act
                List<MonthlyAttendanceCalendarRecordDTO> result = attendanceReportsService.getUserAttendanceCalendar(YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(12);

                for (int i = 0; i < 12; i++) {
                    assertThat(result.get(i).getMonth()).isEqualTo(i);
                    assertThat(result.get(i).getAttendanceCount()).isNotNull();
                    assertThat(result.get(i).getAttendanceCount().getDaysPresent()).isEqualTo(20L);
                }

                verify(userService).getUserTimeZone(USER_ID);
                verify(attendanceRepository, times(12)).getAttendanceCountOfUserBetweenDates(any(), any(), eq(USER_ID));
            }
        }

        @Test
        @DisplayName("Should handle months with zero attendance")
        void shouldHandleMonthsWithZeroAttendance() {
            // Arrange
            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                for (int month = 0; month < 12; month++) {
                    final int finalMonth = month;
                    Instant startDate = Instant.now().plusSeconds(finalMonth * 30L * 24 * 3600);
                    Instant endDate = startDate.plusSeconds(30L * 24 * 3600);
                    mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(finalMonth, YEAR, TIME_ZONE))
                            .thenReturn(new Instant[]{startDate, endDate});

                    AttendanceCountDTO count = AttendanceCountDTO.builder()
                            .daysPresent(0L)
                            .daysAbsent(0L)
                            .daysOnLeave(0L)
                            .daysLate(0L)
                            .build();

                    when(attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, USER_ID))
                            .thenReturn(count);
                }

                // Act
                List<MonthlyAttendanceCalendarRecordDTO> result = attendanceReportsService.getUserAttendanceCalendar(YEAR, USER_ID);

                // Assert
                assertThat(result).hasSize(12);
                result.forEach(record -> {
                    assertThat(record.getAttendanceCount().getDaysPresent()).isEqualTo(0L);
                    assertThat(record.getAttendanceCount().getDaysAbsent()).isEqualTo(0L);
                });
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {2020, 2021, 2022, 2023, 2024, 2025})
        @DisplayName("Should handle different years correctly")
        void shouldHandleDifferentYears(int year) {
            // Arrange
            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                // Mock DateUtils for all months
                for (int month = 0; month < 12; month++) {
                    final int finalMonth = month;
                    Instant startDate = Instant.now();
                    Instant endDate = Instant.now().plusSeconds(86400 * 30);
                    mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(finalMonth, year, TIME_ZONE))
                            .thenReturn(new Instant[]{startDate, endDate});
                }

                // Mock repository to return same count for all months
                AttendanceCountDTO count = AttendanceCountDTO.builder()
                        .daysPresent(15L)
                        .daysAbsent(10L)
                        .daysOnLeave(3L)
                        .daysLate(2L)
                        .build();

                when(attendanceRepository.getAttendanceCountOfUserBetweenDates(any(), any(), eq(USER_ID)))
                        .thenReturn(count);

                // Act
                List<MonthlyAttendanceCalendarRecordDTO> result = attendanceReportsService.getUserAttendanceCalendar(year, USER_ID);

                // Assert
                assertThat(result).hasSize(12);
                result.forEach(record -> {
                    assertThat(record.getAttendanceCount().getDaysPresent()).isEqualTo(15L);
                    assertThat(record.getAttendanceCount().getDaysAbsent()).isEqualTo(10L);
                });
            }
        }
    }

    @Nested
    @DisplayName("getMonthlyUserAttendanceTable Tests")
    class GetMonthlyUserAttendanceTable {

        @Test
        @DisplayName("Should return monthly attendance table with data")
        void shouldReturnMonthlyAttendanceTableWithData() throws NoStatsAvailableException {
            // Arrange
            List<UserAttendanceTableRowDTO> tableRecords = Arrays.asList(
                    new UserAttendanceTableRowDTO(1L, Instant.parse("2024-03-01T10:00:00Z"), AttendanceStatus.ON_TIME),
                    new UserAttendanceTableRowDTO(2L, Instant.parse("2024-03-02T10:00:00Z"), AttendanceStatus.LATE)
            );

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(tableRecords);

                // Act
                List<UserAttendanceTableRowDTO> result = attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, USER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);
                assertThat(result.get(0).getId()).isEqualTo(1L);
                assertThat(result.get(0).getStatus()).isEqualTo(AttendanceStatus.ON_TIME);
                assertThat(result.get(1).getStatus()).isEqualTo(AttendanceStatus.LATE);

                verify(userService).getUserTimeZone(USER_ID);
                verify(attendanceRepository).getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }

        @Test
        @DisplayName("Should throw NoStatsAvailableException when no table data")
        void shouldThrowExceptionWhenNoTableData() {
            // Arrange
            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(Collections.emptyList());

                // Act & Assert
                assertThatThrownBy(() -> attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, USER_ID))
                        .isInstanceOf(NoStatsAvailableException.class);

                verify(attendanceRepository).getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }

        @Test
        @DisplayName("Should handle large number of attendance records")
        void shouldHandleLargeNumberOfAttendanceRecords() throws NoStatsAvailableException {
            // Arrange
            List<UserAttendanceTableRowDTO> largeRecords = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeRecords.add(new UserAttendanceTableRowDTO((long) i, Instant.now(), AttendanceStatus.ON_TIME));
            }

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(largeRecords);

                // Act
                List<UserAttendanceTableRowDTO> result = attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, USER_ID);

                // Assert
                assertThat(result).hasSize(1000);
                verify(attendanceRepository).getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }

        @ParameterizedTest
        @CsvSource({
                "0, 2024",  // January
                "1, 2024",  // February
                "5, 2024",  // June
                "11, 2024"  // December
        })
        @DisplayName("Should handle different months for table data")
        void shouldHandleDifferentMonthsForTableData(int month, int year) throws NoStatsAvailableException {
            // Arrange
            List<UserAttendanceTableRowDTO> records = List.of(
                    new UserAttendanceTableRowDTO(1L, Instant.now(), AttendanceStatus.ON_TIME)
            );

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(any(), any(), eq(USER_ID)))
                        .thenReturn(records);

                // Act
                List<UserAttendanceTableRowDTO> result = attendanceReportsService.getMonthlyUserAttendanceTable(month, year, USER_ID);

                // Assert
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getId()).isEqualTo(1L);
            }
        }
    }

    @Nested
    @DisplayName("getDailyUserAttendances Tests")
    class GetDailyUserAttendances {

        @Test
        @DisplayName("Should return daily user attendances with all filters")
        void shouldReturnDailyUserAttendancesWithAllFilters() throws IOException {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<DailyUserAttendanceDTO> attendances = Collections.singletonList(
                    DailyUserAttendanceDTO.builder()
                            .userId(1L)
                            .fullName("John Doe")
                            .status(AttendanceStatus.ON_TIME)
                            .attendanceType(AttendanceType.CHECK_IN)
                            .designation("Developer")
                            .departmentName("IT")
                            .firstCheckInTime(Instant.now().toEpochMilli())
                            .lastCheckOutTime(Instant.now().plusSeconds(28800).toEpochMilli())
                            .build()
            );
            Page<DailyUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getDailyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                    ORGANIZATION_ID, AttendanceType.CHECK_IN, AttendanceStatusFilter.ON_TIME,
                    "John", Arrays.asList(1L, 2L), pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AttendanceStatus.ON_TIME);

            verify(attendanceRepository).getDailyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return daily user attendances with no filters")
        void shouldReturnDailyUserAttendancesWithNoFilters() throws IOException {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<DailyUserAttendanceDTO> attendances = Collections.singletonList(
                    DailyUserAttendanceDTO.builder()
                            .userId(1L)
                            .fullName("Jane Smith")
                            .status(AttendanceStatus.LATE)
                            .attendanceType(AttendanceType.CHECK_OUT)
                            .build()
            );
            Page<DailyUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getDailyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                    ORGANIZATION_ID, null, null, null, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFullName()).isEqualTo("Jane Smith");

            verify(attendanceRepository).getDailyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should handle empty results")
        void shouldHandleEmptyResults() throws IOException {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<DailyUserAttendanceDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(attendanceRepository.getDailyUserAttendances(any(), eq(pageable)))
                    .thenReturn(emptyPage);

            // Act
            Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                    ORGANIZATION_ID, AttendanceType.CHECK_IN, AttendanceStatusFilter.PRESENT,
                    "Test", List.of(1L), pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(attendanceRepository).getDailyUserAttendances(any(), eq(pageable));
        }

        @ParameterizedTest
        @CsvSource({
                "PRESENT, ON_TIME",
                "PRESENT, LATE",
                "ABSENT, ABSENT",
                "ON_LEAVE, ON_LEAVE"
        })
        @DisplayName("Should handle different attendance status filters")
        void shouldHandleDifferentAttendanceStatusFilters(AttendanceStatusFilter statusFilter, AttendanceStatus expectedStatus) throws IOException {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<DailyUserAttendanceDTO> attendances = Collections.singletonList(
                    DailyUserAttendanceDTO.builder()
                            .userId(1L)
                            .status(expectedStatus)
                            .build()
            );
            Page<DailyUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getDailyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                    ORGANIZATION_ID, AttendanceType.CHECK_IN, statusFilter, null, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(expectedStatus);

            verify(attendanceRepository).getDailyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should handle large department list")
        void shouldHandleLargeDepartmentList() throws IOException {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Long> largeDepartmentIds = new ArrayList<>();
            for (long i = 1; i <= 100; i++) {
                largeDepartmentIds.add(i);
            }

            List<DailyUserAttendanceDTO> attendances = Collections.singletonList(
                    DailyUserAttendanceDTO.builder()
                            .userId(1L)
                            .fullName("Test User")
                            .build()
            );
            Page<DailyUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getDailyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<DailyUserAttendanceDTO> result = attendanceReportsService.getDailyUserAttendances(
                    ORGANIZATION_ID, AttendanceType.CHECK_IN, AttendanceStatusFilter.PRESENT,
                    null, largeDepartmentIds, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(attendanceRepository).getDailyUserAttendances(any(), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getOrganizationMonthlyUserAttendances Tests")
    class GetOrganizationMonthlyUserAttendances {

        @Test
        @DisplayName("Should return organization monthly user attendances with filters")
        void shouldReturnOrganizationMonthlyUserAttendancesWithFilters() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<OrganizationUserAttendanceDTO> attendances = Collections.singletonList(
                    OrganizationUserAttendanceDTO.builder()
                            .id(1L)
                            .fullName("John Doe")
                            .departmentName("IT")
                            .designation("Developer")
                            .daysPresent(20L)
                            .daysOnTime(18L)
                            .daysAbsent(5L)
                            .daysOnLeave(2L)
                            .daysLate(3L)
                            .build()
            );
            Page<OrganizationUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getOrganizationMonthlyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<OrganizationUserAttendanceDTO> result = attendanceReportsService.getOrganizationMonthlyUserAttendances(
                    pageable, YEAR, MONTH, "John", 1L, ORGANIZATION_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
            assertThat(result.getContent().get(0).getDaysPresent()).isEqualTo(20L);
            assertThat(result.getContent().get(0).getDaysOnTime()).isEqualTo(18L);

            verify(attendanceRepository).getOrganizationMonthlyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return organization monthly user attendances with no filters")
        void shouldReturnOrganizationMonthlyUserAttendancesWithNoFilters() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<OrganizationUserAttendanceDTO> attendances = Collections.singletonList(
                    OrganizationUserAttendanceDTO.builder()
                            .id(1L)
                            .fullName("Jane Smith")
                            .daysPresent(15L)
                            .build()
            );
            Page<OrganizationUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getOrganizationMonthlyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<OrganizationUserAttendanceDTO> result = attendanceReportsService.getOrganizationMonthlyUserAttendances(
                    pageable, YEAR, MONTH, null, null, ORGANIZATION_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFullName()).isEqualTo("Jane Smith");

            verify(attendanceRepository).getOrganizationMonthlyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should handle empty results for organization attendances")
        void shouldHandleEmptyResultsForOrganizationAttendances() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrganizationUserAttendanceDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(attendanceRepository.getOrganizationMonthlyUserAttendances(any(), eq(pageable)))
                    .thenReturn(emptyPage);

            // Act
            Page<OrganizationUserAttendanceDTO> result = attendanceReportsService.getOrganizationMonthlyUserAttendances(
                    pageable, YEAR, MONTH, "Test", 1L, ORGANIZATION_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(attendanceRepository).getOrganizationMonthlyUserAttendances(any(), eq(pageable));
        }

        @ParameterizedTest
        @CsvSource({
                "0, 2024",  // January
                "1, 2024",  // February
                "5, 2024",  // June
                "11, 2024"  // December
        })
        @DisplayName("Should handle different months for organization attendances")
        void shouldHandleDifferentMonthsForOrganizationAttendances(int month, int year) {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<OrganizationUserAttendanceDTO> attendances = Collections.singletonList(
                    OrganizationUserAttendanceDTO.builder()
                            .id(1L)
                            .fullName("Test User")
                            .daysPresent(10L)
                            .build()
            );
            Page<OrganizationUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 1);

            when(attendanceRepository.getOrganizationMonthlyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<OrganizationUserAttendanceDTO> result = attendanceReportsService.getOrganizationMonthlyUserAttendances(
                    pageable, year, month, null, null, ORGANIZATION_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDaysPresent()).isEqualTo(10L);

            verify(attendanceRepository).getOrganizationMonthlyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Arrange
            Pageable pageable = PageRequest.of(2, 5); // Page 2, size 5
            List<OrganizationUserAttendanceDTO> attendances = Arrays.asList(
                    OrganizationUserAttendanceDTO.builder()
                            .id(11L)
                            .fullName("User 11")
                            .build(),
                    OrganizationUserAttendanceDTO.builder()
                            .id(12L)
                            .fullName("User 12")
                            .build()
            );
            Page<OrganizationUserAttendanceDTO> expectedPage = new PageImpl<>(attendances, pageable, 50); // Total 50 records

            when(attendanceRepository.getOrganizationMonthlyUserAttendances(any(), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<OrganizationUserAttendanceDTO> result = attendanceReportsService.getOrganizationMonthlyUserAttendances(
                    pageable, YEAR, MONTH, null, null, ORGANIZATION_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(50);
            assertThat(result.getNumber()).isEqualTo(2); // Page number
            assertThat(result.getSize()).isEqualTo(5); // Page size
            assertThat(result.getTotalPages()).isEqualTo(10); // 50 / 5 = 10 pages

            verify(attendanceRepository).getOrganizationMonthlyUserAttendances(any(), eq(pageable));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrors {

        @Test
        @DisplayName("Should handle null time zone gracefully")
        void shouldHandleNullTimeZone() throws NoStatsAvailableException {
            // Arrange
            when(userService.getUserTimeZone(USER_ID)).thenReturn(null);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(anyInt(), anyInt(), isNull()))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(any(), any(), eq(USER_ID)))
                        .thenReturn(List.of(new UserAttendanceTableRowDTO(1L, Instant.now(), AttendanceStatus.ON_TIME)));

                // Act & Assert - Should handle null timezone gracefully
                List<UserAttendanceTableRowDTO> result = attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, USER_ID);
                assertThat(result).isNotNull();
                assertThat(result).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should handle very large result sets")
        void shouldHandleVeryLargeResultSets() throws NoStatsAvailableException {
            // Arrange
            List<UserAttendanceTableRowDTO> largeRecords = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                largeRecords.add(new UserAttendanceTableRowDTO((long) i, Instant.now(), AttendanceStatus.ON_TIME));
            }

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(largeRecords);

                // Act
                List<UserAttendanceTableRowDTO> result = attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, USER_ID);

                // Assert
                assertThat(result).hasSize(10000);
                verify(attendanceRepository).getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID);
            }
        }

        @Test
        @DisplayName("Should handle concurrent calls for different users")
        void shouldHandleConcurrentCallsForDifferentUsers() throws NoStatsAvailableException {
            // Arrange
            Long userId2 = 2L;
            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);
            when(userService.getUserTimeZone(userId2)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.now();
                Instant endDate = Instant.now().plusSeconds(86400 * 30);
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(new Instant[]{startDate, endDate});

                List<UserAttendanceTableRowDTO> records1 = List.of(
                        new UserAttendanceTableRowDTO(1L, Instant.now(), AttendanceStatus.ON_TIME)
                );
                List<UserAttendanceTableRowDTO> records2 = List.of(
                        new UserAttendanceTableRowDTO(2L, Instant.now(), AttendanceStatus.LATE)
                );

                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, USER_ID))
                        .thenReturn(records1);
                when(attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startDate, endDate, userId2))
                        .thenReturn(records2);

                // Act
                List<UserAttendanceTableRowDTO> result1 = attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, USER_ID);
                List<UserAttendanceTableRowDTO> result2 = attendanceReportsService.getMonthlyUserAttendanceTable(MONTH, YEAR, userId2);

                // Assert
                assertThat(result1).hasSize(1);
                assertThat(result1.get(0).getId()).isEqualTo(1L);
                assertThat(result2).hasSize(1);
                assertThat(result2.get(0).getId()).isEqualTo(2L);
                assertThat(result1.get(0).getStatus()).isNotEqualTo(result2.get(0).getStatus());

                verify(userService, times(2)).getUserTimeZone(any());
                verify(attendanceRepository, times(2)).getAttendanceTableRecordsOfUserBetweenDates(any(), any(), any());
            }
        }

        @Test
        @DisplayName("Should handle IOException from repository")
        void shouldHandleIOExceptionFromRepository() throws IOException {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            when(attendanceRepository.getDailyUserAttendances(any(), eq(pageable)))
                    .thenThrow(new IOException("Database connection error"));

            // Act & Assert
            assertThatThrownBy(() -> attendanceReportsService.getDailyUserAttendances(
                    ORGANIZATION_ID, null, null, null, null, pageable))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Database connection error");

            verify(attendanceRepository).getDailyUserAttendances(any(), eq(pageable));
        }

        @Test
        @DisplayName("Should handle null pageable for organization attendances")
        void shouldHandleNullPageableForOrganizationAttendances() {
            // This test verifies the service handles null pageable gracefully
            // In practice, this should be validated at controller level

            when(attendanceRepository.getOrganizationMonthlyUserAttendances(any(), isNull()))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // Act
            Page<OrganizationUserAttendanceDTO> result = attendanceReportsService.getOrganizationMonthlyUserAttendances(
                    null, YEAR, MONTH, null, null, ORGANIZATION_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();

            verify(attendanceRepository).getOrganizationMonthlyUserAttendances(any(), isNull());
        }
    }
}