package com.main.face_recognition_resource_server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.main.face_recognition_resource_server.DTOS.user.*;
import com.main.face_recognition_resource_server.constants.user.UserRole;
import com.main.face_recognition_resource_server.exceptions.*;
import com.main.face_recognition_resource_server.services.department.DepartmentService;
import com.main.face_recognition_resource_server.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("users")
public class UserController {
    private final UserService userService;
    private final DepartmentService departmentService;

    public UserController(UserService userService, DepartmentService departmentService) {
        this.userService = userService;
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> ownUserData(Authentication authentication) throws UserDoesntExistException {
        UserDTO user = userService.getUserDataByUsername(authentication.getName());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(@RequestParam int page, @RequestParam int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserDTO> usersPage = userService.getAllUsers(pageRequest);
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }

    @GetMapping("organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsersOfOwnOrganizationRecordDTO>> usersOfOwnOrganization(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) Boolean isSourceFacePictureRegistered,
            @RequestParam(required = false) String identificationNumber,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UsersOfOwnOrganizationRecordDTO> adminUsersTableRecordDTOPage = userService.getUsersPageOfOrganization(organizationId, pageRequest, fullName, departmentId, designation, isSourceFacePictureRegistered, identificationNumber);
        return new ResponseEntity<>(adminUsersTableRecordDTOPage, HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<HttpStatus> registerUser(@RequestBody RegisterUserDTO userToRegister, Authentication authentication)
            throws DepartmentDoesntExistException,
            DepartmentDoesntBelongToYourOrganizationException,
            UserAlreadyExistsException,
            SQLException,
            IOException,
            UserDoesntExistException, UserAlreadyExistsWithIdentificationNumberException {
        if (userToRegister.getRole() == UserRole.ROLE_USER) {
            Long organizationId = userService.getUserOrganizationId(authentication.getName());
            departmentService.checkIfDepartmentBelongsToOrganization(userToRegister.getDepartmentId(), organizationId);
            userService.registerUser(userToRegister, organizationId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        departmentService.checkIfDepartmentBelongsToOrganization(userToRegister.getDepartmentId(), organizationId);
        userService.registerUser(userToRegister, organizationId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDataDTO> userDataOfId(@PathVariable Long userId, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        userService.checkIfOrganizationBelongsToUser(userId, organizationId);
        UserDataDTO userData = userService.getUserData(userId);
        return new ResponseEntity<>(userData, HttpStatus.OK);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SearchUserDTO>> searchUserByName(@RequestParam String name, Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<SearchUserDTO> users = userService.searchUserByNameOfOrganization(name, organizationId);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/shifts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ShiftAllocationDTO>> userShiftAllocationsOfOwnOrganization(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long shiftId,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication
    ) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ShiftAllocationDTO> shiftAllocationDTOPage = userService.getUserShiftAllocations(organizationId, fullName, departmentId, shiftId, pageRequest);
        return new ResponseEntity<>(shiftAllocationDTOPage, HttpStatus.OK);
    }

    @PostMapping("/change/shifts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> changeUserShiftAllocations(@RequestBody List<EditedShiftAllocationDTO> editedShiftAllocations, Authentication authentication) throws UserDoesntExistException, OrganizationDoesntBelongToYouException, JsonProcessingException, InvalidShiftSelectionException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        this.userService.changeUserShiftAllocations(editedShiftAllocations, organizationId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("register/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> registerUsersViaCSV(@RequestParam("file") MultipartFile file, Authentication authentication) throws UserAlreadyExistsWithIdentificationNumberException, SQLException, UserAlreadyExistsException, IOException, UserDoesntExistException {
        log.info("Inside registerUsersViaCSV");
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        String content = new String(file.getBytes());
        userService.registerUserViaCSV(content, organizationId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
