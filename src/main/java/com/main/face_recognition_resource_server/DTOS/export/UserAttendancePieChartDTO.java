package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserAttendancePieChartDTO implements ExcelChartDTO {
    private Long userId;
    private String fullName;
    private Long onTime;
    private Long late;
    private Long absent;
    private Long onLeave;

    UserAttendancePieChartDTO(Long userId, String firstName, String secondName, Long onTime, Long late, Long absent, Long onLeave) {
        this.userId = userId;
        this.fullName = firstName + " " + secondName;
        this.onTime = onTime;
        this.late = late;
        this.absent = absent;
        this.onLeave = onLeave;
    }
}