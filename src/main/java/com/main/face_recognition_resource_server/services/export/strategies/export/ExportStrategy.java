package com.main.face_recognition_resource_server.services.export.strategies.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelAttendanceChartDTO;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.List;

public interface ExportStrategy {
    ByteArrayResource getAttendanceWorkBook(List<AttendanceExcelDataDTO> attendanceExcelData, List<ExcelAttendanceChartDTO> attendanceChartData) throws IOException;
}