package com.main.face_recognition_resource_server.services.export.strategies.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.constants.export.AttendanceExportStrategyType;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategyFactory;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategyFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@ExportStrategyKey(AttendanceExportStrategyType.DIVISIONS_RANGE)
public class DivisionsRangeAttendanceExport implements AttendanceExportStrategy<DepartmentAttendanceLineChartDTO> {
    private final ExcelSheetStrategyFactory excelSheetStrategyFactory;
    private final ExcelChartStrategyFactory excelChartStrategyFactory;

    public DivisionsRangeAttendanceExport(ExcelSheetStrategyFactory excelSheetStrategyFactory, ExcelChartStrategyFactory excelChartStrategyFactory) {
        this.excelSheetStrategyFactory = excelSheetStrategyFactory;
        this.excelChartStrategyFactory = excelChartStrategyFactory;
    }

    @Override
    public ByteArrayResource getAttendanceWorkBook(List<AttendanceExcelDataDTO> attendanceExcelData, List<DepartmentAttendanceLineChartDTO> attendanceChartData) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        excelSheetStrategyFactory.getStrategy(ExcelSheetCreationStrategyType.CHECK_IN_CHECK_OUT_SHEET).create(workbook, attendanceExcelData);
        excelSheetStrategyFactory.getStrategy(ExcelSheetCreationStrategyType.ATTENDANCE_ANALYTICS_SHEET).create(workbook, attendanceExcelData);
        ExcelChartStrategy<DepartmentAttendanceLineChartDTO> chartStrategy = excelChartStrategyFactory.getExcelChartStrategy(ExcelChartStrategyType.DEPARTMENT_ATTENDANCE_LINE_CHART);
        chartStrategy.create(workbook, attendanceChartData);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }

    @Override
    public Class<DepartmentAttendanceLineChartDTO> getExcelChartDTOClass() {
        return DepartmentAttendanceLineChartDTO.class;
    }
}
