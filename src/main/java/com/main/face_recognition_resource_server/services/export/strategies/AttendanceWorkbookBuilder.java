package com.main.face_recognition_resource_server.services.export.strategies;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategyFactory;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategyFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.List;

public class AttendanceWorkbookBuilder {
    private final XSSFWorkbook workbook;
    private final ExcelSheetStrategyFactory sheetFactory;
    private final ExcelChartStrategyFactory chartFactory;

    private AttendanceWorkbookBuilder(ExcelSheetStrategyFactory sheetFactory, ExcelChartStrategyFactory chartFactory) {
        this.workbook = new XSSFWorkbook();
        this.sheetFactory = sheetFactory;
        this.chartFactory = chartFactory;
    }

    public static AttendanceWorkbookBuilder newWorkbook(ExcelSheetStrategyFactory sheetFactory, ExcelChartStrategyFactory chartFactory) {
        return new AttendanceWorkbookBuilder(sheetFactory, chartFactory);
    }

    public AttendanceWorkbookBuilder addSheet(ExcelSheetCreationStrategyType sheetType, List<AttendanceExcelDataDTO> attendanceExcelData) {
        sheetFactory.getStrategy(sheetType).create(workbook, attendanceExcelData);
        return this;
    }

    public <T extends ExcelChartDTO> AttendanceWorkbookBuilder addChart(ExcelChartStrategyType chartType, List<T> chartData) {
        ExcelChartStrategy<T> chartStrategy = chartFactory.getExcelChartStrategy(chartType);
        chartStrategy.create(workbook, chartData);
        return this;
    }

    public ByteArrayResource build() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } finally {
            workbook.close();
        }
    }
}
