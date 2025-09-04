package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import com.main.face_recognition_resource_server.services.export.strategies.ExportFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public ExportController(ExportFactory exportFactory) {
        this.exportFactory = exportFactory;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportAttendanceExcelFile(@RequestBody ExportAttendanceExcelDataPropsDTO exportExcelProps) throws IOException {
        ByteArrayResource attendanceWorkBook = exportFactory.getExcelFile(exportExcelProps);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.xls\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).contentLength(attendanceWorkBook.contentLength()).body(attendanceWorkBook);
    }
}
