package com.main.face_recognition_resource_server.constants.export;

import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import lombok.Getter;

@Getter
public enum AttendanceExportStrategyType {
    DIVISIONS_RANGE(DepartmentAttendanceLineChartDTO.class),
    DIVISIONS_SINGLE(UserAttendancePieChartDTO.class),
    MEMBERS_RANGE(UserAttendancePieChartDTO.class),
    MEMBERS_SINGLE();

    private final Class<? extends ExcelChartDTO> excelChartDTOClass;

    AttendanceExportStrategyType(Class<? extends ExcelChartDTO> excelChartDTOClass) {
        this.excelChartDTOClass = excelChartDTOClass;
    }

    AttendanceExportStrategyType() {
        this.excelChartDTOClass = null;
    }
}
