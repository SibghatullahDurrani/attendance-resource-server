package com.main.face_recognition_resource_server.services.attendance.stats;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCountDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.DepartmentAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.OrganizationAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceStatsDTO;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.department.DepartmentService;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceStatsServiceImpl implements AttendanceStatsService {
    private final UserService userService;
    private final AttendanceRepository attendanceRepository;
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;

    public AttendanceStatsServiceImpl(UserService userService, AttendanceRepository attendanceRepository, OrganizationService organizationService, DepartmentService departmentService) {
        this.userService = userService;
        this.attendanceRepository = attendanceRepository;
        this.organizationService = organizationService;
        this.departmentService = departmentService;
    }

    @Override
    public UserAttendanceStatsDTO getUserAttendanceStats(int year, Long userId) {
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfYearInTimeZone(year, timeZone);
        return generateAttendanceStatsDTO(startAndEndDate[0], startAndEndDate[1], userId);
    }

    @Override
    public UserAttendanceStatsDTO getUserAttendanceStats(int month, int year, Long userId) {
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
        return generateAttendanceStatsDTO(startAndEndDate[0], startAndEndDate[1], userId);
    }


    private UserAttendanceStatsDTO generateAttendanceStatsDTO(Instant startDate, Instant endDate, Long userId) {
        AttendanceCountDTO attendanceCount = attendanceRepository.getAttendanceCountOfUserBetweenDates(startDate, endDate, userId);
        return new UserAttendanceStatsDTO(attendanceCount);
    }

    @Override
    public OrganizationAttendanceStatsDTO getCurrentDayOrganizationAttendanceStatistics(Long organizationId) {
        String timeZone = organizationService.getOrganizationTimeZone(organizationId);
        Instant today = DateUtils.getStartDateOfToday(timeZone);
        return attendanceRepository.getOrganizationAttendanceStatisticsForDate(organizationId, today);
    }

    @Override
    public List<DepartmentAttendanceStatsDTO> getOrganizationDepartmentsAttendanceStats(Long organizationId, int year, int month, int day) {
        List<Long> departmentIds = departmentService.getDepartmentIdsOfOrganization(organizationId);
        List<DepartmentAttendanceStatsDTO> departmentAttendances = new ArrayList<>();
        String timeZone = organizationService.getOrganizationTimeZone(organizationId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(day, month, year, timeZone);
        for (Long departmentId : departmentIds) {
            Optional<DepartmentAttendanceStatsDTO> departmentAttendance = attendanceRepository.getDepartmentAttendance(departmentId, startAndEndDate[0], startAndEndDate[1]);
            departmentAttendance.ifPresent(departmentAttendances::add);
        }
        return departmentAttendances;
    }
}

