package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExcelAttendanceChartDTO {
    private Long departmentId;
    private String departmentName;
    private List<ExcelDepartmentAttendanceCountDTO> excelDepartmentAttendances;
}
