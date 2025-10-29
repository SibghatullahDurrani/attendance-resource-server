package com.main.face_recognition_resource_server.controllers.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.MonthlyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntBelongToYouException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.graphs.AttendanceGraphsService;
import com.main.face_recognition_resource_server.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("attendances/graphs")
public class AttendanceGraphsController {
    private final AttendanceGraphsService attendanceGraphsService;
    private final UserService userService;

    public AttendanceGraphsController(AttendanceGraphsService attendanceGraphsService, UserService userService) {
        this.attendanceGraphsService = attendanceGraphsService;
        this.userService = userService;
    }

    @GetMapping("/organization/attendance/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyAttendanceGraphDataDTO>> ownOrganizationMonthlyAttendanceGraph(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException {
        Long organizationId = this.userService.getUserOrganizationId(authentication.getName());
        List<DailyAttendanceGraphDataDTO> chartInfos = attendanceGraphsService.getOrganizationAttendanceGraphsData(organizationId, year, month);
        return new ResponseEntity<>(chartInfos, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/attendance/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MonthlyAttendanceGraphDataDTO> userMonthlyAttendanceGraph(@PathVariable Long userId, @RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        MonthlyAttendanceGraphDataDTO userGraphData = attendanceGraphsService.getUserMonthlyAttendanceGraphData(userId, year, month + 1);
        userGraphData.setMonth(userGraphData.getMonth() - 1);
        return new ResponseEntity<>(userGraphData, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/attendance/yearly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MonthlyAttendanceGraphDataDTO>> userYearlyAttendanceGraph(@PathVariable Long userId, @RequestParam int year, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        List<MonthlyAttendanceGraphDataDTO> userGraphData = attendanceGraphsService.getUserYearlyAttendanceGraphData(userId, year);
        return new ResponseEntity<>(userGraphData, HttpStatus.OK);
    }
}