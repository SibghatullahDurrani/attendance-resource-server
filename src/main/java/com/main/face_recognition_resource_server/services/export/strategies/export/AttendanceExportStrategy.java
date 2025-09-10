package com.main.face_recognition_resource_server.services.export.strategies.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.List;

public interface AttendanceExportStrategy<T extends ExcelChartDTO> {
    ByteArrayResource attendanceWorkbook(List<AttendanceExcelDataDTO> attendanceExcelData, List<T> attendanceChartData, ExportAttendanceOptions exportAttendanceOptions) throws IOException;

    Class<T> getExcelChartDTOClass();
}