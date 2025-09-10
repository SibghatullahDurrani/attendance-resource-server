package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DepartmentAttendancePieChartDTO implements ExcelChartDTO {
    private Long departmentId;
    private String departmentName;
    private Long onTime;
    private Long late;
    private Long absent;
    private Long onLeave;
}
