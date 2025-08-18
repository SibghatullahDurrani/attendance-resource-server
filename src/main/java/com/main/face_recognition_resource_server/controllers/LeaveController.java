package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.leave.*;
import com.main.face_recognition_resource_server.constants.LeaveStatus;
import com.main.face_recognition_resource_server.constants.LeaveType;
import com.main.face_recognition_resource_server.exceptions.*;
import com.main.face_recognition_resource_server.services.leave.LeaveService;
import com.main.face_recognition_resource_server.services.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("leaves")
public class LeaveController {
    private final LeaveService leaveService;
    private final UserService userService;

    public LeaveController(LeaveService leaveService, UserService userService) {
        this.leaveService = leaveService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpStatus> requestLeave(@RequestBody LeaveRequestDTO leaveRequest, Authentication authentication) throws UserDoesntExistException, NoMoreLeavesRemainingException {
        leaveService.requestLeave(leaveRequest, authentication.getName());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaveDTO>> getLeavesOfUser(@RequestParam int year, @RequestParam int month, Authentication authentication) throws NoLeaveAvailableException {
        return new ResponseEntity<>(leaveService.getUserLeaves(authentication.getName(), year, month), HttpStatus.OK);
    }

    @GetMapping("remaining-leaves")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RemainingLeavesDTO> getRemainingLeavesOfUser(Authentication authentication) {
        String username = authentication.getName();
        RemainingLeavesDTO remainingLeavesDTO = userService.getRemainingLeavesOfUser(username);
        return new ResponseEntity<>(remainingLeavesDTO, HttpStatus.OK);
    }

    @GetMapping("/organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrganizationLeaveRecordDTO>> getOrganizationLeaves(@RequestParam int page, @RequestParam int size, @RequestParam int year, @RequestParam(required = false) Integer month, @RequestParam(required = false) String userName, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) LeaveType leaveType, @RequestParam(required = false) LeaveStatus leaveStatus, Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<OrganizationLeaveRecordDTO> leaveRecordPage = leaveService.getOrganizationLeavesPage(organizationId, year, month, userName, departmentId, leaveType, leaveStatus, pageRequest);
        return new ResponseEntity<>(leaveRecordPage, HttpStatus.OK);
    }

    @GetMapping("/application/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveApplicationDTO> getLeaveApplication(@PathVariable Long leaveId, Authentication authentication) throws UserDoesntExistException, LeaveDoesntBelongToTheOrganizationException, LeaveDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        leaveService.doesLeaveBelongToOrganization(organizationId, leaveId);
        String leaveApplication = leaveService.getLeaveApplication(leaveId);
        return new ResponseEntity<>(new LeaveApplicationDTO(leaveApplication), HttpStatus.OK);
    }

    @GetMapping("/data/application/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveDataWithApplicationDTO> getLeaveDataWithApplication(@PathVariable Long leaveId, Authentication authentication) throws UserDoesntExistException, LeaveDoesntBelongToTheOrganizationException, LeaveDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        leaveService.doesLeaveBelongToOrganization(organizationId, leaveId);
        LeaveDataWithApplicationDTO leave = leaveService.getLeaveDataWithApplication(leaveId);
        return new ResponseEntity<>(leave, HttpStatus.OK);
    }

    @PostMapping("/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> respondToLeave(@RequestBody RespondToLeaveDTO respondToLeaveDTO, Authentication authentication) throws UserDoesntExistException, LeaveDoesntBelongToTheOrganizationException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        leaveService.doesLeaveBelongToOrganization(organizationId, respondToLeaveDTO.getLeaveId());
        leaveService.respondToLeave(respondToLeaveDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
