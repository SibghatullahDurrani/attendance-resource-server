package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DepartmentAttendancePieChartPreData {
    private Long departmentId;
    private String departmentName;
    private Long onTime;
    private Long late;
    private Long absent;
    private Long onLeave;

    public static DepartmentAttendancePieChartPreData from(DepartmentAttendancePieChartDTO departmentAttendancePieChartDTO) {
        return DepartmentAttendancePieChartPreData.builder()
                .departmentId(departmentAttendancePieChartDTO.getDepartmentId())
                .departmentName(departmentAttendancePieChartDTO.getDepartmentName())
                .onTime(departmentAttendancePieChartDTO.getOnTime())
                .late(departmentAttendancePieChartDTO.getLate())
                .absent(departmentAttendancePieChartDTO.getAbsent())
                .onLeave(departmentAttendancePieChartDTO.getOnLeave())
                .build();
    }
}
