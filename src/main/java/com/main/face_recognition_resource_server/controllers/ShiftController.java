package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.shift.ShiftServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("shifts")
public class ShiftController {

    private final UserServices userServices;
    private final ShiftServices shiftServices;

    public ShiftController(UserServices userServices, ShiftServices shiftServices) {
        this.userServices = userServices;
        this.shiftServices = shiftServices;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> registerShift(@RequestBody RegisterShiftDTO registerShiftDTO, Authentication authentication) throws UserDoesntExistException, SQLException {
        Long organizationId = userServices.getUserOrganizationId(authentication.getName());
        shiftServices.registerShift(registerShiftDTO, organizationId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ShiftTableRowDTO>> getShiftTable(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String checkInTime,
            @RequestParam(required = false) String checkOutTime,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userServices.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ShiftTableRowDTO> shiftTablePage = shiftServices.getShiftsPage(organizationId, name, checkInTime, checkOutTime, pageRequest);
        return new ResponseEntity<>(shiftTablePage, HttpStatus.OK);
    }
}
