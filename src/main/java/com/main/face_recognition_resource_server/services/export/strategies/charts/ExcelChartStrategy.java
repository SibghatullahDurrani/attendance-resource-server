package com.main.face_recognition_resource_server.services.export.strategies.charts;

import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface ExcelChartStrategy<T extends ExcelChartDTO> {
    void create(XSSFWorkbook workbook, List<T> excelAttendanceChartData, String timeZone);

    Class<T> getExcelChartDTOClass();
}
