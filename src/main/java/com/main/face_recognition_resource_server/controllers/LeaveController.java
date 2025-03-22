package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveRequestDTO;
import com.main.face_recognition_resource_server.exceptions.NoLeaveAvailableException;
import com.main.face_recognition_resource_server.exceptions.NoMoreLeavesRemainingException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.leave.LeaveServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("leaves")
public class LeaveController {
  private final LeaveServices leaveServices;

  public LeaveController(LeaveServices leaveServices) {
    this.leaveServices = leaveServices;
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<HttpStatus> requestLeave(@RequestBody LeaveRequestDTO leaveRequest, Authentication authentication) throws UserDoesntExistException, NoMoreLeavesRemainingException {
    leaveServices.requestLeave(leaveRequest, authentication.getName());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<LeaveDTO>> getLeavesOfUser(@RequestParam int year, @RequestParam int month, Authentication authentication) throws NoLeaveAvailableException {
    return new ResponseEntity<>(leaveServices.getUserLeaves(authentication.getName(), year, month), HttpStatus.OK);
  }

}
