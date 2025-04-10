package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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
    return null;
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
}