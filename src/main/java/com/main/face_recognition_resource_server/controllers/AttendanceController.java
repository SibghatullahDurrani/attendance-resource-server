package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.UserAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.UserAttendanceRequestDTO;
import com.main.face_recognition_resource_server.exceptions.AttendanceDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("attendances")
public class AttendanceController {
  private final AttendanceServices attendanceServices;

  public AttendanceController(AttendanceServices attendanceServices) {
    this.attendanceServices = attendanceServices;
  }

  @GetMapping("user")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<UserAttendanceDTO> getUserAttendanceOnDate(@RequestBody UserAttendanceRequestDTO userAttendanceRequestDTO) throws UserDoesntExistException, AttendanceDoesntExistException {
    return new ResponseEntity<>(attendanceServices.getAttendanceOfUserOnDate(userAttendanceRequestDTO.getUserId(), userAttendanceRequestDTO.getDate()), HttpStatus.OK);
  }
}
