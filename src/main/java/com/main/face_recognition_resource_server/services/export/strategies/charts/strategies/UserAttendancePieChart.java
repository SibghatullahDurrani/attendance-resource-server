package com.main.face_recognition_resource_server.services.export.strategies.charts.strategies;

import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelAttendancePieChart;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.charts.ExcelChartStrategyKey;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ExcelChartStrategyKey(ExcelChartStrategyType.USER_ATTENDANCE_PIE_CHART)
public class UserAttendancePieChart implements ExcelChartStrategy<UserAttendancePieChartDTO> {
    private final ExcelAttendancePieChart excelAttendancePieChart;

    public UserAttendancePieChart(ExcelAttendancePieChart excelAttendancePieChart) {
        this.excelAttendancePieChart = excelAttendancePieChart;
    }

    @Override
    public void create(XSSFWorkbook workbook, List<UserAttendancePieChartDTO> excelAttendanceChartData) {
        Long totalOnTime = 0L;
        Long totalLate = 0L;
        Long totalAbsent = 0L;
        Long totalOnLeave = 0L;
        for (UserAttendancePieChartDTO userData : excelAttendanceChartData) {
            excelAttendancePieChart.createPieChart(
                    workbook,
                    userData.getFullName(),
                    userData.getOnTime(),
                    userData.getLate(),
                    userData.getAbsent(),
                    userData.getOnLeave()
            );
            totalOnTime += userData.getOnTime();
            totalLate += userData.getLate();
            totalAbsent += userData.getAbsent();
            totalOnLeave += userData.getOnLeave();
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
    public Class<UserAttendancePieChartDTO> getExcelChartDTOClass() {
        return UserAttendancePieChartDTO.class;
    }
}
