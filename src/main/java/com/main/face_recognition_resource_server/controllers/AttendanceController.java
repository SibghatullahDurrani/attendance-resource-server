package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntBelongToYouException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
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
  private final AttendanceServices attendanceServices;
  private final UserServices userServices;

  public AttendanceController(AttendanceServices attendanceServices, UserServices userServices) {
    this.attendanceServices = attendanceServices;
    this.userServices = userServices;
  }

  @GetMapping("stats")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceStatsDTO> getUserAttendanceStats(@RequestParam int year, @RequestParam(required = false) Integer month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    AttendanceStatsDTO attendanceStatsDTO;
    if (month != null) {
      attendanceStatsDTO = attendanceServices.getUserAttendanceStats(month, year, userId);
    } else {
      attendanceStatsDTO = attendanceServices.getUserAttendanceStats(year, userId);
    }
    return new ResponseEntity<>(attendanceStatsDTO, HttpStatus.OK);
  }

  @GetMapping("calendar")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceCalendarDTO> getUserAttendanceCalendar(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    AttendanceCalendarDTO attendanceCalendar = attendanceServices.getUserAttendanceCalendar(month, year, userId);
    return new ResponseEntity<>(attendanceCalendar, HttpStatus.OK);
  }

  @GetMapping("calendar/yearly")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<MonthlyAttendanceCalendarRecordDTO>> getYearlyUserAttendanceCalendar(@RequestParam int year, Authentication authentication) {
    List<MonthlyAttendanceCalendarRecordDTO> attendanceCalendarRecordDTOS = attendanceServices.getYearlyUserAttendanceCalendar(year, authentication.getName());
    return new ResponseEntity<>(attendanceCalendarRecordDTOS, HttpStatus.OK);
  }

  @GetMapping("overview")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<UserAttendanceDTO>> getUserAttendanceOverview(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    List<UserAttendanceDTO> attendances = attendanceServices.getMonthlyUserAttendanceOverview(month, year, userId);
    return new ResponseEntity<>(attendances, HttpStatus.OK);
  }

  @GetMapping("snaps")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceSnapshotDTO> getUserAttendanceSnapshots(@RequestParam int year, @RequestParam int month, @RequestParam int day, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    AttendanceSnapshotDTO attendanceSnapshot = attendanceServices.getUserAttendanceSnapshots(year, month, day, userId);
    return new ResponseEntity<>(attendanceSnapshot, HttpStatus.OK);
  }

  @GetMapping("table/monthly")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<UserAttendanceTableDTO>> getMonthlyUserAttendance(@RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, NoStatsAvailableException, IOException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    List<UserAttendanceTableDTO> attendances = attendanceServices.getMonthlyUserAttendanceTable(month, year, userId);
    return new ResponseEntity<>(attendances, HttpStatus.OK);
  }

  @GetMapping("yearly")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<UserAttendanceDTO>> getYearlyUserAttendance(@RequestParam int page, @RequestParam int size, @RequestParam int year, Authentication authentication) throws UserDoesntExistException {
    Long userId = userServices.getUserIdByUsername(authentication.getName());
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<UserAttendanceDTO> attendances = attendanceServices.getYearlyUserAttendanceTable(pageRequest, year, userId);
    return new ResponseEntity<>(attendances, HttpStatus.OK);
  }

  @GetMapping("/recent")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<AttendanceLiveFeedDTO>> getRecentAttendances(Authentication authentication) throws UserDoesntExistException {
    long organizationId = userServices.getUserOrganizationId(authentication.getName());
    List<AttendanceLiveFeedDTO> recentAttendances = attendanceServices.getRecentAttendancesOfOrganization(organizationId);
    return new ResponseEntity<>(recentAttendances, HttpStatus.OK);
  }

  @GetMapping("/organization/{organizationId}/departments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<DepartmentAttendanceDTO>> getOrganizationDepartmentsAttendance(@PathVariable Long organizationId, @RequestParam int year, @RequestParam int month, @RequestParam int day, Authentication authentication) throws OrganizationDoesntBelongToYouException, UserDoesntExistException, DepartmentDoesntExistException {
    String username = authentication.getName();
    userServices.checkIfOrganizationBelongsToUser(organizationId, username);
    List<DepartmentAttendanceDTO> organizationDepartmentsAttendance = attendanceServices.getOrganizationDepartmentsAttendance(organizationId, year, month, day);
    return new ResponseEntity<>(organizationDepartmentsAttendance, HttpStatus.OK);
  }

  @GetMapping("/organization/{organizationId}/today")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<DailyUserAttendanceDTO>> getOrganizationDailyUserAttendance(
          @PathVariable Long organizationId,
          @RequestParam(required = false) AttendanceType attendanceType,
          @RequestParam(required = false) AttendanceStatus attendanceStatus,
          @RequestParam(required = false) String userName,
          @RequestParam(required = false) String departmentName,
          @RequestParam int page,
          @RequestParam int size,
          Authentication authentication) throws OrganizationDoesntBelongToYouException, UserDoesntExistException {
    userServices.checkIfOrganizationBelongsToUser(organizationId, authentication.getName());
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<DailyUserAttendanceDTO> dailyUserAttendances = attendanceServices.getDailyUserAttendances(organizationId, attendanceType, attendanceStatus, userName, departmentName, pageRequest);
    return new ResponseEntity<>(dailyUserAttendances, HttpStatus.OK);
  }

  @GetMapping("/organization/{organizationId}/attendance-graphs-data")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<DailyAttendanceGraphDataDTO>> getOrganizationAttendanceGraphsData(@PathVariable Long organizationId, @RequestParam int year, @RequestParam int month, Authentication authentication) throws OrganizationDoesntBelongToYouException, UserDoesntExistException {
    userServices.checkIfOrganizationBelongsToUser(organizationId, authentication.getName());
    List<DailyAttendanceGraphDataDTO> chartInfos = attendanceServices.getOrganizationAttendanceGraphsData(organizationId, year, month);
    return new ResponseEntity<>(chartInfos, HttpStatus.OK);
  }

  @GetMapping("/user/{userId}/monthly-attendance-graph-data")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MonthlyAttendanceGraphDataDTO> getUserMonthlyAttendanceGraphData(@PathVariable Long userId, @RequestParam int year, @RequestParam int month, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    userServices.checkIfOrganizationBelongsToUser(userId, organizationId);
    MonthlyAttendanceGraphDataDTO userGraphData = attendanceServices.getUserMonthlyAttendanceGraphData(userId, year, month + 1);
    userGraphData.setMonth(userGraphData.getMonth() - 1);
    return new ResponseEntity<>(userGraphData, HttpStatus.OK);
  }

  @GetMapping("/user/{userId}/yearly-attendance-graph-data")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<MonthlyAttendanceGraphDataDTO>> getUserYearlyAttendanceGraphData(@PathVariable Long userId, @RequestParam int year, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    userServices.checkIfOrganizationBelongsToUser(userId, organizationId);
    List<MonthlyAttendanceGraphDataDTO> userGraphData = attendanceServices.getUserYearlyAttendanceGraphData(userId, year);
    return new ResponseEntity<>(userGraphData, HttpStatus.OK);
  }
}