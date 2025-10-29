package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.export.*;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.export.ExportService;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("export")
public class ExportController {
    private final ExportFactory exportFactory;
    private final UserService userService;
    private final ExportService exportService;

    public ExportController(ExportFactory exportFactory, UserService userService, ExportService exportService, ExportService exportService1) {
        this.exportFactory = exportFactory;
        this.userService = userService;
        this.exportService = exportService1;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportAttendanceExcelFile(@RequestBody ExportExcelDataPropsDTO exportExcelProps, Authentication authentication) throws IOException, UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        ByteArrayResource attendanceWorkBook = exportFactory.getExcelFile(exportExcelProps, organizationId);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.xls\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).contentLength(attendanceWorkBook.contentLength()).body(attendanceWorkBook);
    }

    @GetMapping("pre-data/all-single-day")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AllSingleDayPreDataDTO> allSingleDayPreData(@RequestParam Long date, Authentication authentication) throws UserDoesntExistException {
        Long organizationId = userService.getUserOrganizationId(authentication.getName());
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getOrganizationExcelData(organizationId, date);
        List<DepartmentAttendancePieChartDTO> departmentAttendancePieChart = exportService.getOrganizationPieChartData(organizationId, date);
        AllSingleDayPreDataDTO allSingleDayPreDataDTO = AllSingleDayPreDataDTO.builder()
                .departmentAttendancePieChartPreData(
                        departmentAttendancePieChart.stream().map(DepartmentAttendancePieChartPreData::from).toList()
                )
                .attendanceData(
                        excelAttendanceData.stream().map(AttendancePreDataDTO::from).toList()
                )
                .build();
        return ResponseEntity.ok().body(allSingleDayPreDataDTO);
    }
}
