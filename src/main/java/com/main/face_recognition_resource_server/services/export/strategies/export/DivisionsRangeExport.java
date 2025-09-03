package com.main.face_recognition_resource_server.services.export.strategies.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelAttendanceChartDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import com.main.face_recognition_resource_server.constants.export.ExportStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.charts.LineChart;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategyFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@ExportStrategyKey(ExportStrategyType.DIVISIONS_RANGE)
public class DivisionsRangeExport implements ExportStrategy {
    private final ExcelSheetStrategyFactory excelSheetStrategyFactory;
    private final LineChart chart;

    public DivisionsRangeExport(ExcelSheetStrategyFactory excelSheetStrategyFactory, LineChart chart) {
        this.excelSheetStrategyFactory = excelSheetStrategyFactory;
        this.chart = chart;
    }

    @Override
    public ByteArrayResource getAttendanceWorkBook(List<AttendanceExcelDataDTO> attendanceExcelData, List<ExcelAttendanceChartDTO> attendanceChartData) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        excelSheetStrategyFactory.getStrategy(ExcelSheetCreationStrategyType.CHECK_IN_CHECK_OUT_SHEET).create(workbook, attendanceExcelData);
        excelSheetStrategyFactory.getStrategy(ExcelSheetCreationStrategyType.ATTENDANCE_ANALYTICS_SHEET).create(workbook, attendanceExcelData);
        chart.create(workbook, attendanceChartData);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }
}
