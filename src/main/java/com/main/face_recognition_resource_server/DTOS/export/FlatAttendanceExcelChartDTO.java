package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FlatAttendanceExcelChartDTO {
    private Long departmentId;
    private String departmentName;
    private Instant date;
    private Long onTime;
    private Long late;
    private Long absent;
    private Long onLeave;
}
