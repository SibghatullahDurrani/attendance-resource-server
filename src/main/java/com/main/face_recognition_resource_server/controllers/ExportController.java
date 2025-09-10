package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.export.ExportExcelDataPropsDTO;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.export.strategies.ExportFactory;
import com.main.face_recognition_resource_server.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("export")
public class ExportController {
    private final ExportFactory exportFactory;
    private final UserService userService;

    public ExportController(ExportFactory exportFactory, UserService userService) {
        this.exportFactory = exportFactory;
        this.userService = userService;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportAttendanceExcelFile(@RequestBody ExportExcelDataPropsDTO exportExcelProps, Authentication authentication) throws IOException, UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        ByteArrayResource attendanceWorkBook = exportFactory.getExcelFile(exportExcelProps, organizationId);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.xls\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).contentLength(attendanceWorkBook.contentLength()).body(attendanceWorkBook);
    }
}
