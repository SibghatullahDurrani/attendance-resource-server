package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExcelAttendanceDTO {
    private Date date;
    private Date checkIn;
    private Date checkOut;
    private AttendanceStatus attendanceStatus;
    private Long attendanceId;

    ExcelAttendanceDTO(Date date, AttendanceStatus attendanceStatus, Long attendanceId) {
        this.date = date;
        this.attendanceStatus = attendanceStatus;
        this.attendanceId = attendanceId;
    }
}
