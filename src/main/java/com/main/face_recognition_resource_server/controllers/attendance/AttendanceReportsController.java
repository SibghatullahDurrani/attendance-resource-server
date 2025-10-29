package com.main.face_recognition_resource_server.controllers.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntBelongToYouException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.reports.AttendanceReportsService;
import com.main.face_recognition_resource_server.services.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("attendances/reports")
public class AttendanceReportsController {
    private final UserService userService;
    private final AttendanceReportsService attendanceReportsService;

    public AttendanceReportsController(UserService userService, AttendanceReportsService attendanceReportsService) {
        this.userService = userService;
        this.attendanceReportsService = attendanceReportsService;
    }

    @GetMapping("calendar/monthly")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MonthlyAttendanceCalendarDTO> getUserMonthlyAttendanceCalendar(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        MonthlyAttendanceCalendarDTO attendanceCalendar = attendanceReportsService.getUserAttendanceCalendar(month, year, userId);
        return new ResponseEntity<>(attendanceCalendar, HttpStatus.OK);
    }

    @GetMapping("calendar/yearly")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MonthlyAttendanceCalendarRecordDTO>> getUserYearlyAttendanceCalendar(@RequestParam int year, Authentication authentication) throws UserDoesntExistException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        List<MonthlyAttendanceCalendarRecordDTO> attendanceCalendarRecordDTOS = attendanceReportsService.getUserAttendanceCalendar(year, userId);
        return new ResponseEntity<>(attendanceCalendarRecordDTOS, HttpStatus.OK);
    }

    @GetMapping("/own/monthly")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserAttendanceTableRowDTO>> getOwnMonthlyAttendances(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException, IOException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        List<UserAttendanceTableRowDTO> attendances = attendanceReportsService.getMonthlyUserAttendanceTable(month, year, userId);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

//    @GetMapping("yearly")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<Page<UserAttendanceDTO>> getYearlyUserAttendance(@RequestParam int page, @RequestParam int size, @RequestParam int year, Authentication authentication) throws UserDoesntExistException {
//        Long userId = userService.getUserIdByUsername(authentication.getName());
//        PageRequest pageRequest = PageRequest.of(page, size);
//        Page<UserAttendanceDTO> attendances = attendanceService.getYearlyUserAttendanceTable(pageRequest, year, userId);
//        return new ResponseEntity<>(attendances, HttpStatus.OK);
//    }

    @GetMapping("organization/users/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DailyUserAttendanceDTO>> getOrganizationCurrentDayUserAttendances(
            @RequestParam(required = false) AttendanceType attendanceType,
            @RequestParam(required = false) AttendanceStatusFilter attendanceStatus,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) List<Long> departmentIds,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication) throws UserDoesntExistException, IOException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<DailyUserAttendanceDTO> dailyUserAttendances = attendanceReportsService.getDailyUserAttendances(organizationId, attendanceType, attendanceStatus, userName, departmentIds, pageRequest);
        return new ResponseEntity<>(dailyUserAttendances, HttpStatus.OK);
    }

    @GetMapping("{userId}/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAttendanceTableRowDTO>> getUserMonthlyAttendances(@PathVariable Long userId, @RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException, NoStatsAvailableException, IOException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        List<UserAttendanceTableRowDTO> userAttendanceTable = attendanceReportsService.getMonthlyUserAttendanceTable(month, year, userId);
        return new ResponseEntity<>(userAttendanceTable, HttpStatus.OK);
    }

    @GetMapping("/organization/users/monthly")
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
        Page<OrganizationUserAttendanceDTO> organizationUserAttendances = attendanceReportsService.getOrganizationMonthlyUserAttendances(pageRequest, year, month, fullName, departmentId, organizationId);
        return new ResponseEntity<>(organizationUserAttendances, HttpStatus.OK);
    }

    @PostMapping("organization/check-in-check-out-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CheckInCheckOutReportRecordDTO>> checkInCheckOutReport(
            @RequestBody AttendanceReportRequestParams attendanceReportRequestParams,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(attendanceReportRequestParams.getPage(), attendanceReportRequestParams.getSize());
        Page<CheckInCheckOutReportRecordDTO> checkInCheckOutReportPage = attendanceReportsService.getCheckInCheckOutReportPageOfOrganization(pageRequest, organizationId, attendanceReportRequestParams);
        return new ResponseEntity<>(checkInCheckOutReportPage, HttpStatus.OK);
    }

    @PostMapping("organization/attendance-analytics-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceAnalyticsReportRecordDTO>> attendanceAnalyticsReport(
            @RequestBody AttendanceReportRequestParams attendanceReportRequestParams,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(attendanceReportRequestParams.getPage(), attendanceReportRequestParams.getSize());
        Page<AttendanceAnalyticsReportRecordDTO> attendanceAnalyticsReportPage = attendanceReportsService.getAttendanceAnalyticsReportPageOfOrganization(pageRequest, organizationId, attendanceReportRequestParams);
        return new ResponseEntity<>(attendanceAnalyticsReportPage, HttpStatus.OK);
    }
}
