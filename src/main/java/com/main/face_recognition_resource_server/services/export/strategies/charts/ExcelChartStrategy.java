package com.main.face_recognition_resource_server.services.export.strategies.charts;

import com.main.face_recognition_resource_server.DTOS.export.ExcelAttendanceChartDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface ExcelChartStrategy {
    void create(XSSFWorkbook workbook, List<ExcelAttendanceChartDTO> excelAttendanceChartData);
}
