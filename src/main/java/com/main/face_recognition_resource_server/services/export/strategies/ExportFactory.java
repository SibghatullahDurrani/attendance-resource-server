package com.main.face_recognition_resource_server.services.export.strategies;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import com.main.face_recognition_resource_server.constants.export.AttendanceExportStrategyType;
import com.main.face_recognition_resource_server.services.export.ExportService;
import com.main.face_recognition_resource_server.services.export.strategies.export.AttendanceExportStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.export.AttendanceExportStrategyFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ExportFactory {
    private final ExportService exportService;
    private final AttendanceExportStrategyFactory attendanceExportStrategyFactory;

    public ExportFactory(ExportService exportService, AttendanceExportStrategyFactory attendanceExportStrategyFactory) {
        this.exportService = exportService;
        this.attendanceExportStrategyFactory = attendanceExportStrategyFactory;
    }

    public ByteArrayResource getExcelFile(ExportAttendanceExcelDataPropsDTO exportExcelProps) throws IOException {
        switch (exportExcelProps.getExportMode()) {
            case DIVISIONS_RANGE -> {
                List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getDepartmentAttendanceExcelData(exportExcelProps.getDepartmentIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
                List<DepartmentAttendanceLineChartDTO> excelAttendanceChartData = exportService.getDepartmentsAttendanceLineChartData(exportExcelProps.getDepartmentIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
                AttendanceExportStrategy<DepartmentAttendanceLineChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.DIVISIONS_RANGE);
                return attendanceExportStrategy.getAttendanceWorkBook(excelAttendanceData, excelAttendanceChartData);
            }
            case MEMBERS_RANGE -> {
                List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getUserAttendanceExcelData(exportExcelProps.getUserIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
                List<UserAttendancePieChartDTO> userAttendancePieChartData = exportService.getUserAttendancePieChartData(exportExcelProps.getUserIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
                AttendanceExportStrategy<UserAttendancePieChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.MEMBERS_RANGE);
                return attendanceExportStrategy.getAttendanceWorkBook(excelAttendanceData, userAttendancePieChartData);
            }
            default -> {
                return null;
            }
        }
    }
}
