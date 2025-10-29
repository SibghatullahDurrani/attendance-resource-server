package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DepartmentAttendanceStatsDTO {
    private Long id;
    private String departmentName;
    private Long present;
    private Long late;
    private Long absent;
    private Long onLeave;
    private Long onTime;
    private Long total;

    public DepartmentAttendanceStatsDTO(Long present, Long late, Long absent, Long onLeave, Long onTime) {
        this.present = present;
        this.late = late;
        this.absent = absent;
        this.onLeave = onLeave;
        this.onTime = onTime;
    }
}
