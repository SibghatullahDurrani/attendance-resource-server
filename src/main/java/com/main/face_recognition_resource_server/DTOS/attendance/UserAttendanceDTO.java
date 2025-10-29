package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class UserAttendanceDTO {
    private Long id;
    private Long date;
    private AttendanceStatus status;
    private List<Long> checkIns;
    private List<Long> checkOuts;

    public UserAttendanceDTO(Long id, Instant date, AttendanceStatus status) {
        this.id = id;
        this.date = date.toEpochMilli();
        this.status = status;
    }
}
