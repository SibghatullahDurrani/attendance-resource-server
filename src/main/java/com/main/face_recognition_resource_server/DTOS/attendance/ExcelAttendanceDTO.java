package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExcelAttendanceDTO {
    private Instant date;
    private Instant checkIn;
    private Instant checkOut;
    private AttendanceStatus attendanceStatus;
    private Long attendanceId;

    ExcelAttendanceDTO(Instant date, AttendanceStatus attendanceStatus, Long attendanceId) {
        this.date = date;
        this.attendanceStatus = attendanceStatus;
        this.attendanceId = attendanceId;
    }
}
