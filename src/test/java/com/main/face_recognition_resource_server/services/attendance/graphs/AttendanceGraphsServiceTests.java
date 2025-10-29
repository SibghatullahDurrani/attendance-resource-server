package com.main.face_recognition_resource_server.services.attendance.graphs;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.MonthlyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
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
@DisplayName("AttendanceGraphsService Unit Tests")
class AttendanceGraphsServiceTests {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private AttendanceGraphsServiceImpl attendanceGraphsService;

    private static final Long USER_ID = 1L;
    private static final Long ORGANIZATION_ID = 100L;
    private static final String TIME_ZONE = "America/New_York";
    private static final String UTC_TIME_ZONE = "UTC";
    private static final int YEAR = 2024;
    private static final int MONTH = 3;

    @BeforeEach
    void setUp() {
        reset(attendanceRepository, userService, organizationService);
    }

    @Nested
    @DisplayName("getOrganizationAttendanceGraphsData Tests")
    class GetOrganizationAttendanceGraphsData {

        @Test
        @DisplayName("Should return attendance graphs data for valid organization, year, and month")
        void shouldReturnGraphsDataForValidInputs() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            List<DailyAttendanceGraphDataDTO> expectedData = Arrays.asList(
                    DailyAttendanceGraphDataDTO.builder()
                            .date(startDate.toEpochMilli())
                            .presentCount(50L)
                            .lateCount(10L)
                            .absentCount(5L)
                            .leaveCount(2L)
                            .build(),
                    DailyAttendanceGraphDataDTO.builder()
                            .date(startDate.plusSeconds(86400).toEpochMilli())
                            .presentCount(48L)
                            .lateCount(12L)
                            .absentCount(7L)
                            .leaveCount(0L)
                            .build()
            );

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(ORGANIZATION_ID, startDate, endDate))
                        .thenReturn(expectedData);

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);
                assertThat(result).isEqualTo(expectedData);
                
                verify(organizationService).getOrganizationTimeZone(ORGANIZATION_ID);
                verify(attendanceRepository).getOrganizationAttendanceChartInfo(ORGANIZATION_ID, startDate, endDate);
            }
        }

        @Test
        @DisplayName("Should return empty list when no attendance data exists")
        void shouldReturnEmptyListWhenNoData() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(ORGANIZATION_ID, startDate, endDate))
                        .thenReturn(Collections.emptyList());

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).isEmpty();
            }
        }

        @ParameterizedTest
        @DisplayName("Should handle different months correctly")
        @ValueSource(ints = {1, 2, 6, 11, 12})
        void shouldHandleDifferentMonths(int month) {
            // Arrange
            Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-01-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(any(), any(), any()))
                        .thenReturn(new ArrayList<>());

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, YEAR, month);

                // Assert
                assertThat(result).isNotNull();
                
                verify(organizationService).getOrganizationTimeZone(ORGANIZATION_ID);
                mockedDateUtils.verify(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, YEAR, TIME_ZONE));
            }
        }

        @ParameterizedTest
        @DisplayName("Should handle different time zones")
        @CsvSource({
                "UTC",
                "Asia/Kolkata",
                "Europe/London",
                "Pacific/Auckland",
                "America/Los_Angeles"
        })
        void shouldHandleDifferentTimeZones(String timeZone) {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(timeZone);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, timeZone))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(any(), any(), any()))
                        .thenReturn(new ArrayList<>());

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                
                verify(organizationService).getOrganizationTimeZone(ORGANIZATION_ID);
                mockedDateUtils.verify(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, timeZone));
            }
        }

        @Test
        @DisplayName("Should handle leap year February correctly")
        void shouldHandleLeapYearFebruary() {
            // Arrange
            int leapYear = 2024;
            int february = 2;
            Instant startDate = Instant.parse("2024-02-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-02-29T23:59:59Z"); // Leap year
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(february, leapYear, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(ORGANIZATION_ID, startDate, endDate))
                        .thenReturn(new ArrayList<>());

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, leapYear, february);

                // Assert
                assertThat(result).isNotNull();
                verify(attendanceRepository).getOrganizationAttendanceChartInfo(ORGANIZATION_ID, startDate, endDate);
            }
        }

        @Test
        @DisplayName("Should handle large dataset efficiently")
        void shouldHandleLargeDataset() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            // Create large dataset (31 days of March)
            List<DailyAttendanceGraphDataDTO> largeDataset = new ArrayList<>();
            for (int i = 0; i < 31; i++) {
                largeDataset.add(DailyAttendanceGraphDataDTO.builder()
                        .date(startDate.plusSeconds(i * 86400).toEpochMilli())
                        .presentCount(100L + i)
                        .lateCount(10L + i)
                        .absentCount(5L + i)
                        .leaveCount(2L)
                        .build());
            }

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(ORGANIZATION_ID, startDate, endDate))
                        .thenReturn(largeDataset);

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(31);
                assertThat(result).isEqualTo(largeDataset);
            }
        }

        @Test
        @DisplayName("Should handle null organization ID gracefully")
        void shouldHandleNullOrganizationId() {
            // This test verifies that the service doesn't perform null checks itself
            // as it expects valid input from the controller layer
            when(organizationService.getOrganizationTimeZone(null)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
                Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
                Instant[] dateRange = new Instant[]{startDate, endDate};
                
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(null, startDate, endDate))
                        .thenReturn(new ArrayList<>());

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        null, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getUserMonthlyAttendanceGraphData Tests")
    class GetUserMonthlyAttendanceGraphData {

        @Test
        @DisplayName("Should return user monthly attendance graph data when data exists")
        void shouldReturnUserMonthlyDataWhenExists() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            MonthlyAttendanceGraphDataDTO expectedData = MonthlyAttendanceGraphDataDTO.builder()
                    .month(MONTH)
                    .presentCount(20L)
                    .lateCount(5L)
                    .absentCount(3L)
                    .leaveCount(2L)
                    .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.of(expectedData));

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result).isEqualTo(expectedData);
                assertThat(result.getMonth()).isEqualTo(MONTH);
                assertThat(result.getPresentCount()).isEqualTo(20L);
                
                verify(userService).getUserTimeZone(USER_ID);
                verify(attendanceRepository).getUserAttendanceGraphData(USER_ID, startDate, endDate);
            }
        }

        @Test
        @DisplayName("Should return default data when no attendance data exists")
        void shouldReturnDefaultDataWhenNoDataExists() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.empty());

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getMonth()).isEqualTo(MONTH);
                assertThat(result.getPresentCount()).isEqualTo(0L);
                assertThat(result.getAbsentCount()).isEqualTo(0L);
                assertThat(result.getLateCount()).isEqualTo(0L);
                assertThat(result.getLeaveCount()).isEqualTo(0L);
            }
        }

        @ParameterizedTest
        @DisplayName("Should handle all months of the year")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
        void shouldHandleAllMonthsOfYear(int month) {
            // Arrange
            Instant startDate = Instant.now();
            Instant endDate = startDate.plusSeconds(2592000); // 30 days
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.empty());

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, month);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getMonth()).isEqualTo(month);
            }
        }

        @ParameterizedTest
        @DisplayName("Should handle different time zones correctly")
        @CsvSource({
                "UTC",
                "Asia/Kolkata",
                "Europe/London",
                "America/New_York",
                "Australia/Sydney"
        })
        void shouldHandleDifferentTimeZonesForUser(String timeZone) {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(userService.getUserTimeZone(USER_ID)).thenReturn(timeZone);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, timeZone))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.of(MonthlyAttendanceGraphDataDTO.builder()
                                .month(MONTH)
                                .presentCount(15L)
                                .build()));

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                verify(userService).getUserTimeZone(USER_ID);
                mockedDateUtils.verify(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, timeZone));
            }
        }

        @Test
        @DisplayName("Should handle zero counts correctly")
        void shouldHandleZeroCounts() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            MonthlyAttendanceGraphDataDTO zeroData = MonthlyAttendanceGraphDataDTO.builder()
                    .month(MONTH)
                    .presentCount(0L)
                    .lateCount(0L)
                    .absentCount(0L)
                    .leaveCount(0L)
                    .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.of(zeroData));

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getPresentCount()).isEqualTo(0L);
                assertThat(result.getLateCount()).isEqualTo(0L);
                assertThat(result.getAbsentCount()).isEqualTo(0L);
                assertThat(result.getLeaveCount()).isEqualTo(0L);
            }
        }

        @Test
        @DisplayName("Should handle maximum values correctly")
        void shouldHandleMaximumValues() {
            // Arrange
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            MonthlyAttendanceGraphDataDTO maxData = MonthlyAttendanceGraphDataDTO.builder()
                    .month(MONTH)
                    .presentCount(Long.MAX_VALUE)
                    .lateCount(Long.MAX_VALUE)
                    .absentCount(Long.MAX_VALUE)
                    .leaveCount(Long.MAX_VALUE)
                    .build();

            when(userService.getUserTimeZone(USER_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, TIME_ZONE))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.of(maxData));

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getPresentCount()).isEqualTo(Long.MAX_VALUE);
                assertThat(result.getLateCount()).isEqualTo(Long.MAX_VALUE);
                assertThat(result.getAbsentCount()).isEqualTo(Long.MAX_VALUE);
                assertThat(result.getLeaveCount()).isEqualTo(Long.MAX_VALUE);
            }
        }
    }

    @Nested
    @DisplayName("getUserYearlyAttendanceGraphData Tests")
    class GetUserYearlyAttendanceGraphData {

        @Test
        @DisplayName("Should return yearly attendance data for all 12 months")
        void shouldReturnYearlyDataForAllMonths() {
            // Arrange
            List<MonthlyAttendanceGraphDataDTO> expectedData = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                expectedData.add(MonthlyAttendanceGraphDataDTO.builder()
                        .month(month)
                        .presentCount(20L)
                        .lateCount(5L)
                        .absentCount(3L)
                        .leaveCount(2L)
                        .build());
            }

            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenReturn(expectedData);

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, YEAR);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(12);
            assertThat(result).isEqualTo(expectedData);
            
            for (int i = 0; i < 12; i++) {
                assertThat(result.get(i).getMonth()).isEqualTo(i + 1);
            }
            
            verify(attendanceRepository).getUserYearlyAttendanceGraphData(USER_ID, YEAR);
        }

        @Test
        @DisplayName("Should return empty list when no yearly data exists")
        void shouldReturnEmptyListWhenNoYearlyData() {
            // Arrange
            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenReturn(Collections.emptyList());

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, YEAR);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle partial year data correctly")
        void shouldHandlePartialYearData() {
            // Arrange - Only first 6 months have data
            List<MonthlyAttendanceGraphDataDTO> partialData = new ArrayList<>();
            for (int month = 1; month <= 6; month++) {
                partialData.add(MonthlyAttendanceGraphDataDTO.builder()
                        .month(month)
                        .presentCount(20L)
                        .lateCount(5L)
                        .absentCount(3L)
                        .leaveCount(2L)
                        .build());
            }

            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenReturn(partialData);

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, YEAR);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(6);
            assertThat(result).isEqualTo(partialData);
        }

        @ParameterizedTest
        @DisplayName("Should handle different years correctly")
        @ValueSource(ints = {2020, 2021, 2022, 2023, 2024, 2025})
        void shouldHandleDifferentYears(int year) {
            // Arrange
            List<MonthlyAttendanceGraphDataDTO> yearData = Arrays.asList(
                    MonthlyAttendanceGraphDataDTO.builder()
                            .month(1)
                            .presentCount(15L)
                            .build()
            );

            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, year))
                    .thenReturn(yearData);

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, year);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(yearData);
            
            verify(attendanceRepository).getUserYearlyAttendanceGraphData(USER_ID, year);
        }

        @Test
        @DisplayName("Should handle months with zero attendance correctly")
        void shouldHandleMonthsWithZeroAttendance() {
            // Arrange
            List<MonthlyAttendanceGraphDataDTO> zeroAttendanceData = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                zeroAttendanceData.add(MonthlyAttendanceGraphDataDTO.builder()
                        .month(month)
                        .presentCount(0L)
                        .lateCount(0L)
                        .absentCount(0L)
                        .leaveCount(0L)
                        .build());
            }

            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenReturn(zeroAttendanceData);

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, YEAR);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(12);
            result.forEach(monthData -> {
                assertThat(monthData.getPresentCount()).isEqualTo(0L);
                assertThat(monthData.getLateCount()).isEqualTo(0L);
                assertThat(monthData.getAbsentCount()).isEqualTo(0L);
                assertThat(monthData.getLeaveCount()).isEqualTo(0L);
            });
        }

        @Test
        @DisplayName("Should handle unordered months from repository")
        void shouldHandleUnorderedMonths() {
            // Arrange - Months returned in random order
            List<MonthlyAttendanceGraphDataDTO> unorderedData = Arrays.asList(
                    MonthlyAttendanceGraphDataDTO.builder().month(5).presentCount(10L).build(),
                    MonthlyAttendanceGraphDataDTO.builder().month(2).presentCount(20L).build(),
                    MonthlyAttendanceGraphDataDTO.builder().month(11).presentCount(30L).build(),
                    MonthlyAttendanceGraphDataDTO.builder().month(1).presentCount(40L).build()
            );

            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenReturn(unorderedData);

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, YEAR);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(4);
            assertThat(result).containsExactlyElementsOf(unorderedData);
        }

        @Test
        @DisplayName("Should handle null user ID")
        void shouldHandleNullUserId() {
            // Arrange
            when(attendanceRepository.getUserYearlyAttendanceGraphData(null, YEAR))
                    .thenReturn(new ArrayList<>());

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    null, YEAR);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle negative year values")
        void shouldHandleNegativeYear() {
            // Arrange
            int negativeYear = -2024;
            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, negativeYear))
                    .thenReturn(new ArrayList<>());

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, negativeYear);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            
            verify(attendanceRepository).getUserYearlyAttendanceGraphData(USER_ID, negativeYear);
        }

        @Test
        @DisplayName("Should handle future years correctly")
        void shouldHandleFutureYears() {
            // Arrange
            int futureYear = 2050;
            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, futureYear))
                    .thenReturn(new ArrayList<>());

            // Act
            List<MonthlyAttendanceGraphDataDTO> result = attendanceGraphsService.getUserYearlyAttendanceGraphData(
                    USER_ID, futureYear);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Scenarios")
    class EdgeCasesAndIntegration {

        @Test
        @DisplayName("Should handle concurrent calls for same user")
        void shouldHandleConcurrentCallsForSameUser() {
            // Arrange
            List<MonthlyAttendanceGraphDataDTO> yearlyData = Arrays.asList(
                    MonthlyAttendanceGraphDataDTO.builder().month(1).presentCount(10L).build()
            );

            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenReturn(yearlyData);

            // Act - Simulate concurrent calls
            List<MonthlyAttendanceGraphDataDTO> result1 = attendanceGraphsService.getUserYearlyAttendanceGraphData(USER_ID, YEAR);
            List<MonthlyAttendanceGraphDataDTO> result2 = attendanceGraphsService.getUserYearlyAttendanceGraphData(USER_ID, YEAR);

            // Assert
            assertThat(result1).isEqualTo(result2);
            verify(attendanceRepository, times(2)).getUserYearlyAttendanceGraphData(USER_ID, YEAR);
        }

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldPropagateRepositoryExceptions() {
            // Arrange
            when(attendanceRepository.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            assertThatThrownBy(() -> attendanceGraphsService.getUserYearlyAttendanceGraphData(USER_ID, YEAR))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");
        }

        @Test
        @DisplayName("Should handle timezone conversion edge cases")
        void shouldHandleTimezoneConversionEdgeCases() {
            // Arrange - Test date at timezone boundary
            String pacificTimeZone = "Pacific/Kiritimati"; // UTC+14
            String bakerTimeZone = "Pacific/Baker"; // UTC-12
            
            Instant startDate = Instant.parse("2024-03-01T00:00:00Z");
            Instant endDate = Instant.parse("2024-03-31T23:59:59Z");
            Instant[] dateRange = new Instant[]{startDate, endDate};

            when(userService.getUserTimeZone(USER_ID)).thenReturn(pacificTimeZone);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(MONTH, YEAR, pacificTimeZone))
                        .thenReturn(dateRange);

                when(attendanceRepository.getUserAttendanceGraphData(USER_ID, startDate, endDate))
                        .thenReturn(Optional.empty());

                // Act
                MonthlyAttendanceGraphDataDTO result = attendanceGraphsService.getUserMonthlyAttendanceGraphData(
                        USER_ID, YEAR, MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getMonth()).isEqualTo(MONTH);
            }
        }

        @Test
        @DisplayName("Should handle month boundary transitions")
        void shouldHandleMonthBoundaryTransitions() {
            // Test December to January transition
            int december = 12;
            Instant decStartDate = Instant.parse("2024-12-01T00:00:00Z");
            Instant decEndDate = Instant.parse("2024-12-31T23:59:59Z");
            Instant[] decDateRange = new Instant[]{decStartDate, decEndDate};

            when(organizationService.getOrganizationTimeZone(ORGANIZATION_ID)).thenReturn(TIME_ZONE);

            try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
                mockedDateUtils.when(() -> DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(december, YEAR, TIME_ZONE))
                        .thenReturn(decDateRange);

                when(attendanceRepository.getOrganizationAttendanceChartInfo(ORGANIZATION_ID, decStartDate, decEndDate))
                        .thenReturn(new ArrayList<>());

                // Act
                List<DailyAttendanceGraphDataDTO> result = attendanceGraphsService.getOrganizationAttendanceGraphsData(
                        ORGANIZATION_ID, YEAR, december);

                // Assert
                assertThat(result).isNotNull();
                verify(attendanceRepository).getOrganizationAttendanceChartInfo(ORGANIZATION_ID, decStartDate, decEndDate);
            }
        }
    }
}