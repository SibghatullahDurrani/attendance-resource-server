package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelAttendanceChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import com.main.face_recognition_resource_server.services.export.ExportService;
import com.main.face_recognition_resource_server.services.export.strategies.export.ExportStrategy;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("export")
public class ExportController {
    private final ExportService exportService;
    private final ExportStrategy exportStrategy;

    public ExportController(ExportService exportService, ExportStrategy exportStrategy) {
        this.exportService = exportService;
        this.exportStrategy = exportStrategy;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportAttendanceExcelFile(@RequestBody ExportAttendanceExcelDataPropsDTO exportExcelProps) throws IOException {
        List<AttendanceExcelDataDTO> excelDataObject = exportService.getAttendanceExcelData(exportExcelProps);
        List<ExcelAttendanceChartDTO> excelAttendanceChartData = exportService.getDivisionsExcelChartData(exportExcelProps.getDepartmentIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        ByteArrayResource attendanceWorkBook = exportStrategy.getAttendanceWorkBook(excelDataObject, excelAttendanceChartData);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.xls\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).contentLength(attendanceWorkBook.contentLength()).body(attendanceWorkBook);
    }
}
