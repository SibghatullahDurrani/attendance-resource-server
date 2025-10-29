package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftOptionDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.shift.ShiftService;
import com.main.face_recognition_resource_server.services.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("shifts")
public class ShiftController {

    private final UserService userService;
    private final ShiftService shiftService;

    public ShiftController(UserService userService, ShiftService shiftService) {
        this.userService = userService;
        this.shiftService = shiftService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> registerShift(@RequestBody RegisterShiftDTO registerShiftDTO, Authentication authentication) throws UserDoesntExistException, SQLException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        shiftService.registerShift(registerShiftDTO, organizationId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShiftOptionDTO>> shiftOptionsOfOrganization(Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<ShiftOptionDTO> shiftOptions = shiftService.getShiftOptionsOfOrganization(organizationId);
        return new ResponseEntity<>(shiftOptions, HttpStatus.OK);
    }

    @GetMapping("shifts-table")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ShiftTableRowDTO>> shiftsOfOrganization(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String checkInTime,
            @RequestParam(required = false) String checkOutTime,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ShiftTableRowDTO> shiftTablePage = shiftService.getShiftsPageOfOrganization(organizationId, name, checkInTime, checkOutTime, pageRequest);
        return new ResponseEntity<>(shiftTablePage, HttpStatus.OK);
    }

}
