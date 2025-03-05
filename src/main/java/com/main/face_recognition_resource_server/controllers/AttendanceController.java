package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.AttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.UserAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.UserAttendanceRequestDTO;
import com.main.face_recognition_resource_server.exceptions.AttendanceDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("attendances")
public class AttendanceController {
  private final AttendanceServices attendanceServices;
  private final UserServices userServices;

  public AttendanceController(AttendanceServices attendanceServices, UserServices userServices) {
    this.attendanceServices = attendanceServices;
    this.userServices = userServices;
  }

  @GetMapping("user")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<UserAttendanceDTO> getUserAttendanceOnDate(@RequestBody UserAttendanceRequestDTO userAttendanceRequestDTO) throws UserDoesntExistException, AttendanceDoesntExistException {
    return new ResponseEntity<>(attendanceServices.getAttendanceOfUserOnDate(userAttendanceRequestDTO.getUserId(), userAttendanceRequestDTO.getDate()), HttpStatus.OK);
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
}
