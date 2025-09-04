package com.main.face_recognition_resource_server.constants.export;


import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import lombok.Getter;

@Getter
public enum ExcelChartStrategyType {
    DEPARTMENT_ATTENDANCE_LINE_CHART(DepartmentAttendanceLineChartDTO.class),
    USER_ATTENDANCE_PIE_CHART(UserAttendancePieChartDTO.class);

    private final Class<? extends ExcelChartDTO> excelChartDtoType;

    ExcelChartStrategyType(Class<? extends ExcelChartDTO> excelChartDtoType) {
        this.excelChartDtoType = excelChartDtoType;
    }

}