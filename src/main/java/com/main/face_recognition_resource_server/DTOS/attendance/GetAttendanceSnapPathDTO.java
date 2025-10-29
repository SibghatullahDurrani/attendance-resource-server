package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetAttendanceSnapPathDTO {
    private String snapPath;
    private Long attendanceTime;

    GetAttendanceSnapPathDTO(String snapPath, Instant date) {
        this.snapPath = snapPath;
        this.attendanceTime = date.toEpochMilli();
    }
}
