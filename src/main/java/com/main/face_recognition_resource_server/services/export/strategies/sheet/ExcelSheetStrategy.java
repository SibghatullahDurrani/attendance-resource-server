package com.main.face_recognition_resource_server.services.export.strategies.sheet;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface ExcelSheetStrategy {
    void create(XSSFWorkbook workbook, List<AttendanceExcelDataDTO> attendanceExcelData, String timeZone);
}
