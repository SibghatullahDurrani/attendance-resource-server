package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.shift.ShiftServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
