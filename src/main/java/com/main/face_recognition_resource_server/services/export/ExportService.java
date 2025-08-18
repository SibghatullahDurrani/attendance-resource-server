package com.main.face_recognition_resource_server.services.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.List;

public interface ExportService {
    List<AttendanceExcelDataDTO> getAttendanceExcelData(ExportAttendanceExcelDataPropsDTO exportExcelProps);

    ByteArrayResource getAttendanceWorkBook(List<AttendanceExcelDataDTO> attendanceExcelDataDTO) throws IOException;
}
