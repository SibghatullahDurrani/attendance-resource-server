package com.main.face_recognition_resource_server.services.export.strategies.export.strategies;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import com.main.face_recognition_resource_server.constants.export.AttendanceExportStrategyType;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.AttendanceWorkbookBuilder;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategyFactory;
import com.main.face_recognition_resource_server.services.export.strategies.export.AttendanceExportStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.export.ExportAttendanceOptions;
import com.main.face_recognition_resource_server.services.export.strategies.export.ExportStrategyKey;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategyFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@ExportStrategyKey(AttendanceExportStrategyType.MEMBERS_RANGE)
public class MembersRangeAttendanceExport implements AttendanceExportStrategy<UserAttendancePieChartDTO> {
    private final ExcelSheetStrategyFactory excelSheetStrategyFactory;
    private final ExcelChartStrategyFactory excelChartStrategyFactory;

    public MembersRangeAttendanceExport(ExcelSheetStrategyFactory excelSheetStrategyFactory, ExcelChartStrategyFactory excelChartStrategyFactory) {
        this.excelSheetStrategyFactory = excelSheetStrategyFactory;
        this.excelChartStrategyFactory = excelChartStrategyFactory;
    }

    @Override
    public ByteArrayResource attendanceWorkbook(List<AttendanceExcelDataDTO> attendanceExcelData, List<UserAttendancePieChartDTO> attendanceChartData, ExportAttendanceOptions exportAttendanceOptions, String timeZone) throws IOException {
        AttendanceWorkbookBuilder attendanceWorkbookBuilder = AttendanceWorkbookBuilder.newWorkbook(excelSheetStrategyFactory, excelChartStrategyFactory);
        if (exportAttendanceOptions.isIncludeCheckInCheckOutSheet())
            attendanceWorkbookBuilder = attendanceWorkbookBuilder.addSheet(ExcelSheetCreationStrategyType.CHECK_IN_CHECK_OUT_SHEET, attendanceExcelData, timeZone);
        if (exportAttendanceOptions.isIncludeAttendanceSheet())
            attendanceWorkbookBuilder = attendanceWorkbookBuilder.addSheet(ExcelSheetCreationStrategyType.ATTENDANCE_ANALYTICS_SHEET, attendanceExcelData, timeZone);
        if (exportAttendanceOptions.isIncludeGraphs())
            attendanceWorkbookBuilder = attendanceWorkbookBuilder.addChart(ExcelChartStrategyType.USER_ATTENDANCE_PIE_CHART, attendanceChartData, timeZone);

        return attendanceWorkbookBuilder.build();
    }

    @Override
    public Class<UserAttendancePieChartDTO> getExcelChartDTOClass() {
        return UserAttendancePieChartDTO.class;
    }
}
