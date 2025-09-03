package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntBelongToYouException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceService;
import com.main.face_recognition_resource_server.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("attendances")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final UserService userService;

    public AttendanceController(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }

    @GetMapping("stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceStatsDTO> getUserAttendanceStats(@RequestParam int year, @RequestParam(required = false) Integer month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        AttendanceStatsDTO attendanceStatsDTO;
        if (month != null) {
            attendanceStatsDTO = attendanceService.getUserAttendanceStats(month, year, userId);
        } else {
            attendanceStatsDTO = attendanceService.getUserAttendanceStats(year, userId);
        }
        return new ResponseEntity<>(attendanceStatsDTO, HttpStatus.OK);
    }

    @GetMapping("calendar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceCalendarDTO> getUserAttendanceCalendar(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        AttendanceCalendarDTO attendanceCalendar = attendanceService.getUserAttendanceCalendar(month, year, userId);
        return new ResponseEntity<>(attendanceCalendar, HttpStatus.OK);
    }

    @GetMapping("calendar/yearly")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MonthlyAttendanceCalendarRecordDTO>> getYearlyUserAttendanceCalendar(@RequestParam int year, Authentication authentication) {
        List<MonthlyAttendanceCalendarRecordDTO> attendanceCalendarRecordDTOS = attendanceService.getYearlyUserAttendanceCalendar(year, authentication.getName());
        return new ResponseEntity<>(attendanceCalendarRecordDTOS, HttpStatus.OK);
    }

    @GetMapping("overview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserAttendanceDTO>> getUserAttendanceOverview(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        List<UserAttendanceDTO> attendances = attendanceService.getMonthlyUserAttendanceOverview(month, year, userId);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("snaps")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceSnapshotDTO> getUserAttendanceSnapshots(@RequestParam int year, @RequestParam int month, @RequestParam int day, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        AttendanceSnapshotDTO attendanceSnapshot = attendanceService.getUserAttendanceSnapshots(year, month, day, userId);
        return new ResponseEntity<>(attendanceSnapshot, HttpStatus.OK);
    }

    @GetMapping("table/monthly")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserAttendanceTableDTO>> getOwnMonthlyAttendance(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException, IOException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        List<UserAttendanceTableDTO> attendances = attendanceService.getMonthlyUserAttendanceTable(month, year, userId);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("yearly")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserAttendanceDTO>> getYearlyUserAttendance(@RequestParam int page, @RequestParam int size, @RequestParam int year, Authentication authentication) throws UserDoesntExistException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserAttendanceDTO> attendances = attendanceService.getYearlyUserAttendanceTable(pageRequest, year, userId);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceLiveFeedDTO>> getRecentAttendances(Authentication authentication) throws UserDoesntExistException {
        long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<AttendanceLiveFeedDTO> recentAttendances = attendanceService.getRecentAttendancesOfOrganization(organizationId);
        return new ResponseEntity<>(recentAttendances, HttpStatus.OK);
    }

    @GetMapping("/organization/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentAttendanceDTO>> getOrganizationDepartmentsAttendance(@RequestParam int year, @RequestParam int month, @RequestParam int day, Authentication authentication) throws OrganizationDoesntBelongToYouException, UserDoesntExistException, DepartmentDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<DepartmentAttendanceDTO> organizationDepartmentsAttendance = attendanceService.getOrganizationDepartmentsAttendance(organizationId, year, month, day);
        return new ResponseEntity<>(organizationDepartmentsAttendance, HttpStatus.OK);
    }

    @GetMapping("/organization/users/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DailyUserAttendanceDTO>> getOrganizationDailyUserAttendance(
            @RequestParam(required = false) AttendanceType attendanceType,
            @RequestParam(required = false) AttendanceStatusFilter attendanceStatus,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) List<Long> departmentIds,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication) throws UserDoesntExistException, IOException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<DailyUserAttendanceDTO> dailyUserAttendances = attendanceService.getDailyUserAttendances(organizationId, attendanceType, attendanceStatus, userName, departmentIds, pageRequest);
        return new ResponseEntity<>(dailyUserAttendances, HttpStatus.OK);
    }

    @GetMapping("/organization/attendance-graphs-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyAttendanceGraphDataDTO>> getOrganizationAttendanceGraphsData(@RequestParam int year, @RequestParam int month, Authentication authentication) throws OrganizationDoesntBelongToYouException, UserDoesntExistException {
        Long organizationId = this.userService.getUserOrganizationId(authentication.getName());
        List<DailyAttendanceGraphDataDTO> chartInfos = attendanceService.getOrganizationAttendanceGraphsData(organizationId, year, month);
        return new ResponseEntity<>(chartInfos, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/monthly-attendance-graph-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MonthlyAttendanceGraphDataDTO> getUserMonthlyAttendanceGraphData(@PathVariable Long userId, @RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        MonthlyAttendanceGraphDataDTO userGraphData = attendanceService.getUserMonthlyAttendanceGraphData(userId, year, month + 1);
        userGraphData.setMonth(userGraphData.getMonth() - 1);
        return new ResponseEntity<>(userGraphData, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/yearly-attendance-graph-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MonthlyAttendanceGraphDataDTO>> getUserYearlyAttendanceGraphData(@PathVariable Long userId, @RequestParam int year, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        List<MonthlyAttendanceGraphDataDTO> userGraphData = attendanceService.getUserYearlyAttendanceGraphData(userId, year);
        return new ResponseEntity<>(userGraphData, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/monthly-attendance-table")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAttendanceTableDTO>> getUserMonthlyAttendanceTable(@PathVariable Long userId, @RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException, NoStatsAvailableException, IOException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        List<UserAttendanceTableDTO> userAttendanceTable = attendanceService.getMonthlyUserAttendanceTable(month, year, userId);
        return new ResponseEntity<>(userAttendanceTable, HttpStatus.OK);
    }

    @GetMapping("/organization/today-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationAttendanceStatisticsDTO> getCurrentDayOrganizationAttendanceStatistics(Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        OrganizationAttendanceStatisticsDTO statistics = attendanceService.getCurrentDayOrganizationAttendanceStatistics(organizationId);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/organization/monthly-user-attendances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrganizationUserAttendanceDTO>> getOrganizationMonthlyUserAttendances(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) Long departmentId,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<OrganizationUserAttendanceDTO> organizationUserAttendances = attendanceService.getOrganizationMonthlyUserAttendances(pageRequest, year, month, fullName, departmentId, organizationId);
        return new ResponseEntity<>(organizationUserAttendances, HttpStatus.OK);
    }

}