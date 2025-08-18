package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import com.main.face_recognition_resource_server.services.export.ExportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("export")
public class ExportController {
    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportAttendanceExcelFile(@RequestBody ExportAttendanceExcelDataPropsDTO exportExcelProps) throws IOException {
        List<AttendanceExcelDataDTO> excelDataObject = exportService.getAttendanceExcelData(exportExcelProps);
        ByteArrayResource attendanceWorkBook = exportService.getAttendanceWorkBook(excelDataObject);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.xls\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(attendanceWorkBook.contentLength())
                .body(attendanceWorkBook);
    }
}
