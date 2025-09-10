package com.main.face_recognition_resource_server.services.export.strategies;

import com.main.face_recognition_resource_server.DTOS.export.*;
import com.main.face_recognition_resource_server.constants.export.AttendanceExportStrategyType;
import com.main.face_recognition_resource_server.services.export.ExportService;
import com.main.face_recognition_resource_server.services.export.strategies.export.AttendanceExportStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.export.AttendanceExportStrategyFactory;
import com.main.face_recognition_resource_server.services.export.strategies.export.ExportAttendanceOptions;
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

    public ByteArrayResource getExcelFile(ExportExcelDataPropsDTO exportExcelProps, Long organizationId) throws IOException {
        ByteArrayResource attendanceWorkbook;
        ExportAttendanceOptions exportAttendanceOptions = exportExcelProps.getExportAttendanceOptions();
        switch (exportExcelProps.getAttendanceExportMode()) {
            case DIVISIONS_RANGE ->
                    attendanceWorkbook = divisionsRangeAttendanceWorkBook(exportExcelProps, exportAttendanceOptions);
            case DIVISIONS_SINGLE_DAY ->
                    attendanceWorkbook = divisionsSingleDayAttendanceWorkBook(exportExcelProps, exportAttendanceOptions);
            case DIVISIONS_MONTH ->
                    attendanceWorkbook = divisionsMonthAttendanceWorkBook(exportExcelProps, exportAttendanceOptions);
            case MEMBERS_RANGE ->
                    attendanceWorkbook = membersRangeAttendanceWorkBook(exportExcelProps, exportAttendanceOptions);
            case MEMBERS_SINGLE_DAY ->
                    attendanceWorkbook = membersSingleDayAttendanceWorkBook(exportExcelProps, exportAttendanceOptions);
            case MEMBERS_MONTH ->
                    attendanceWorkbook = membersMonthAttendanceWorkBook(exportExcelProps, exportAttendanceOptions);
            case ALL_SINGLE_DAY ->
                    attendanceWorkbook = allSingleDayAttendanceWorkBook(organizationId, exportExcelProps, exportAttendanceOptions);
            case ALL_RANGE ->
                    attendanceWorkbook = allRangeAttendanceWorkBook(organizationId, exportExcelProps, exportAttendanceOptions);
            case ALL_MONTH ->
                    attendanceWorkbook = allMonthAttendanceWorkBook(organizationId, exportExcelProps, exportAttendanceOptions);
            default -> {
                return null;
            }
        }
        return attendanceWorkbook;
    }


    private ByteArrayResource membersMonthAttendanceWorkBook(ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getUserAttendanceExcelData(exportExcelProps.getUserIds(), exportExcelProps.getMonth(), exportExcelProps.getYear());
        List<UserAttendancePieChartDTO> userAttendancePieChartData = exportService.getUserAttendancePieChartData(exportExcelProps.getUserIds(), exportExcelProps.getMonth(), exportExcelProps.getYear());
        AttendanceExportStrategy<UserAttendancePieChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.MEMBERS_MONTH);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, userAttendancePieChartData, exportAttendanceOptions);
    }

    private ByteArrayResource membersRangeAttendanceWorkBook(ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getUserAttendanceExcelData(exportExcelProps.getUserIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        List<UserAttendancePieChartDTO> userAttendancePieChartData = exportService.getUserAttendancePieChartData(exportExcelProps.getUserIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        AttendanceExportStrategy<UserAttendancePieChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.MEMBERS_RANGE);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, userAttendancePieChartData, exportAttendanceOptions);
    }

    private ByteArrayResource membersSingleDayAttendanceWorkBook(ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getUserAttendanceExcelData(exportExcelProps.getUserIds(), exportExcelProps.getSingleDate());
        List<UserAttendancePieChartDTO> userAttendancePieChartData = exportService.getUserAttendancePieChartData(exportExcelProps.getUserIds(), exportExcelProps.getSingleDate());
        AttendanceExportStrategy<UserAttendancePieChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.MEMBERS_SINGLE_DAY);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, userAttendancePieChartData, exportAttendanceOptions);
    }

    private ByteArrayResource divisionsMonthAttendanceWorkBook(ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getDepartmentAttendanceExcelData(exportExcelProps.getDepartmentIds(), exportExcelProps.getMonth(), exportExcelProps.getYear());
        List<DepartmentAttendanceLineChartDTO> excelAttendanceChartData = exportService.getDepartmentsAttendanceLineChartData(exportExcelProps.getDepartmentIds(), exportExcelProps.getMonth(), exportExcelProps.getYear());
        AttendanceExportStrategy<DepartmentAttendanceLineChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.DIVISIONS_MONTH);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, excelAttendanceChartData, exportAttendanceOptions);
    }

    private ByteArrayResource divisionsRangeAttendanceWorkBook(ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getDepartmentAttendanceExcelData(exportExcelProps.getDepartmentIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        List<DepartmentAttendanceLineChartDTO> excelAttendanceChartData = exportService.getDepartmentsAttendanceLineChartData(exportExcelProps.getDepartmentIds(), exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        AttendanceExportStrategy<DepartmentAttendanceLineChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.DIVISIONS_RANGE);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, excelAttendanceChartData, exportAttendanceOptions);
    }

    private ByteArrayResource divisionsSingleDayAttendanceWorkBook(ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getDepartmentAttendanceExcelData(exportExcelProps.getDepartmentIds(), exportExcelProps.getSingleDate());
        List<DepartmentAttendancePieChartDTO> departmentAttendancePieChartData = exportService.getDepartmentAttendancePieChartData(exportExcelProps.getDepartmentIds(), exportExcelProps.getSingleDate());
        AttendanceExportStrategy<DepartmentAttendancePieChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.DIVISIONS_SINGLE_DAY);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, departmentAttendancePieChartData, exportAttendanceOptions);
    }

    private ByteArrayResource allSingleDayAttendanceWorkBook(Long organizationId, ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getOrganizationExcelData(organizationId, exportExcelProps.getSingleDate());
        List<DepartmentAttendancePieChartDTO> organizationAttendancePieChartData = exportService.getOrganizationPieChartData(organizationId, exportExcelProps.getSingleDate());
        AttendanceExportStrategy<DepartmentAttendancePieChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.ALL_SINGLE_DAY);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, organizationAttendancePieChartData, exportAttendanceOptions);
    }

    private ByteArrayResource allRangeAttendanceWorkBook(Long organizationId, ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getOrganizationExcelData(organizationId, exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        List<DepartmentAttendanceLineChartDTO> organizationAttendancePieChartData = exportService.getOrganizationLineChartData(organizationId, exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        AttendanceExportStrategy<DepartmentAttendanceLineChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.ALL_RANGE);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, organizationAttendancePieChartData, exportAttendanceOptions);
    }

    private ByteArrayResource allMonthAttendanceWorkBook(Long organizationId, ExportExcelDataPropsDTO exportExcelProps, ExportAttendanceOptions exportAttendanceOptions) throws IOException {
        List<AttendanceExcelDataDTO> excelAttendanceData = exportService.getOrganizationExcelData(organizationId, exportExcelProps.getMonth(), exportExcelProps.getYear());
        List<DepartmentAttendanceLineChartDTO> organizationAttendancePieChartData = exportService.getOrganizationLineChartData(organizationId, exportExcelProps.getMonth(), exportExcelProps.getYear());
        AttendanceExportStrategy<DepartmentAttendanceLineChartDTO> attendanceExportStrategy = attendanceExportStrategyFactory.getAttendanceExportStrategy(AttendanceExportStrategyType.ALL_MONTH);
        return attendanceExportStrategy.attendanceWorkbook(excelAttendanceData, organizationAttendancePieChartData, exportAttendanceOptions);
    }
}
