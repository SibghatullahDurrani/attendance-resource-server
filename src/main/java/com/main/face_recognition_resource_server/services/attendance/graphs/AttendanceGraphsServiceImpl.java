package com.main.face_recognition_resource_server.services.attendance.graphs;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.MonthlyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AttendanceGraphsServiceImpl implements AttendanceGraphsService {
    private final AttendanceRepository attendanceRepository;
    private final UserService userService;
    private final OrganizationService organizationService;

    public AttendanceGraphsServiceImpl(AttendanceRepository attendanceRepository, UserService userService, OrganizationService organizationService) {
        this.attendanceRepository = attendanceRepository;
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @Override
    public List<DailyAttendanceGraphDataDTO> getOrganizationAttendanceGraphsData(Long organizationId, int year, int month) {
        String timeZone = organizationService.getOrganizationTimeZone(organizationId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
        return attendanceRepository.getOrganizationAttendanceChartInfo(organizationId, startAndEndDate[0], startAndEndDate[1]);
    }

    @Override
    public MonthlyAttendanceGraphDataDTO getUserMonthlyAttendanceGraphData(Long userId, int year, int month) {
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
        Optional<MonthlyAttendanceGraphDataDTO> userAttendanceGraphData = attendanceRepository.getUserAttendanceGraphData(userId, startAndEndDate[0], startAndEndDate[1]);
        return userAttendanceGraphData.orElseGet(() -> MonthlyAttendanceGraphDataDTO.builder().month(month).presentCount(0L).absentCount(0L).lateCount(0L).leaveCount(0L).build());
    }

    @Override
    public List<MonthlyAttendanceGraphDataDTO> getUserYearlyAttendanceGraphData(Long userId, int year) {
        return attendanceRepository.getUserYearlyAttendanceGraphData(userId, year);
    }
}