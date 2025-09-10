package com.main.face_recognition_resource_server.constants.export;

import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import lombok.Getter;

@Getter
public enum AttendanceExportStrategyType {
    DIVISIONS_RANGE(DepartmentAttendanceLineChartDTO.class),
    DIVISIONS_SINGLE_DAY(DepartmentAttendancePieChartDTO.class),
    DIVISIONS_MONTH(DepartmentAttendanceLineChartDTO.class),
    MEMBERS_RANGE(UserAttendancePieChartDTO.class),
    MEMBERS_SINGLE_DAY(UserAttendancePieChartDTO.class),
    MEMBERS_MONTH(UserAttendancePieChartDTO.class),
    ALL_RANGE(DepartmentAttendanceLineChartDTO.class),
    ALL_SINGLE_DAY(DepartmentAttendancePieChartDTO.class),
    ALL_MONTH(DepartmentAttendanceLineChartDTO.class);

    private final Class<? extends ExcelChartDTO> excelChartDTOClass;

    AttendanceExportStrategyType(Class<? extends ExcelChartDTO> excelChartDTOClass) {
        this.excelChartDTOClass = excelChartDTOClass;
    }

    AttendanceExportStrategyType() {
        this.excelChartDTOClass = null;
    }
}
