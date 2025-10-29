package com.main.face_recognition_resource_server.controllers.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.DepartmentAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.OrganizationAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceStatsDTO;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.stats.AttendanceStatsService;
import com.main.face_recognition_resource_server.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("attendances/stats")
public class AttendanceStatsController {
    private final UserService userService;
    private final AttendanceStatsService attendanceStatsService;

    public AttendanceStatsController(UserService userService, AttendanceStatsService attendanceStatsService) {
        this.userService = userService;
        this.attendanceStatsService = attendanceStatsService;
    }

    @GetMapping("user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserAttendanceStatsDTO> userAttendanceStats(@RequestParam int year, @RequestParam(required = false) Integer month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
        Long userId = userService.getUserIdByUsername(authentication.getName());
        UserAttendanceStatsDTO userAttendanceStatsDTO;
        if (month != null) {
            userAttendanceStatsDTO = attendanceStatsService.getUserAttendanceStats(month, year, userId);
        } else {
            userAttendanceStatsDTO = attendanceStatsService.getUserAttendanceStats(year, userId);
        }
        return new ResponseEntity<>(userAttendanceStatsDTO, HttpStatus.OK);
    }

    @GetMapping("/organization/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationAttendanceStatsDTO> organizationCurrentDayAttendanceStats(Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        OrganizationAttendanceStatsDTO statistics = attendanceStatsService.getCurrentDayOrganizationAttendanceStatistics(organizationId);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/organization/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentAttendanceStatsDTO>> organizationDepartmentsAttendanceStats(@RequestParam int year, @RequestParam int month, @RequestParam int day, Authentication authentication) throws UserDoesntExistException, DepartmentDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<DepartmentAttendanceStatsDTO> organizationDepartmentsAttendance = attendanceStatsService.getOrganizationDepartmentsAttendanceStats(organizationId, year, month, day);
        return new ResponseEntity<>(organizationDepartmentsAttendance, HttpStatus.OK);
    }
}
