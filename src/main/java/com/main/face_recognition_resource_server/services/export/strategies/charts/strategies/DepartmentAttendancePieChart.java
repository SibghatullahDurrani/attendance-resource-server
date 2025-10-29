package com.main.face_recognition_resource_server.services.export.strategies.charts.strategies;

import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelAttendancePieChart;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategyKey;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ExcelChartStrategyKey(ExcelChartStrategyType.DEPARTMENT_ATTENDANCE_PIE_CHART)
public class DepartmentAttendancePieChart implements ExcelChartStrategy<DepartmentAttendancePieChartDTO> {
    private final ExcelAttendancePieChart excelAttendancePieChart;

    public DepartmentAttendancePieChart(ExcelAttendancePieChart excelAttendancePieChart) {
        this.excelAttendancePieChart = excelAttendancePieChart;
    }

    @Override
    public void create(XSSFWorkbook workbook, List<DepartmentAttendancePieChartDTO> excelAttendanceChartData, String timeZone) {
        Long totalOnTime = 0L;
        Long totalLate = 0L;
        Long totalAbsent = 0L;
        Long totalOnLeave = 0L;
        for (DepartmentAttendancePieChartDTO departmentAttendancePieChartData : excelAttendanceChartData) {
            excelAttendancePieChart.createPieChart(
                    workbook,
                    departmentAttendancePieChartData.getDepartmentName(),
                    departmentAttendancePieChartData.getOnTime(),
                    departmentAttendancePieChartData.getLate(),
                    departmentAttendancePieChartData.getAbsent(),
                    departmentAttendancePieChartData.getOnLeave()
            );
            totalOnTime += departmentAttendancePieChartData.getOnTime();
            totalLate += departmentAttendancePieChartData.getLate();
            totalAbsent += departmentAttendancePieChartData.getAbsent();
            totalOnLeave += departmentAttendancePieChartData.getOnLeave();
        }
        excelAttendancePieChart.createPieChart(
                workbook,
                "Overall Attendance",
                totalOnTime,
                totalLate,
                totalAbsent,
                totalOnLeave
        );
    }

    @Override
    public Class<DepartmentAttendancePieChartDTO> getExcelChartDTOClass() {
        return DepartmentAttendancePieChartDTO.class;
    }
}
