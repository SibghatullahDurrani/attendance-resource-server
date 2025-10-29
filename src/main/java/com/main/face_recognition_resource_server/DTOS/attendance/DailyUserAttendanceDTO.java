package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DailyUserAttendanceDTO {
    private Long userId;
    private String fullName;
    private AttendanceStatus status;
    private AttendanceType attendanceType;
    private String designation;
    private String departmentName;
    private Long firstCheckInTime;
    private Long lastCheckOutTime;

    public DailyUserAttendanceDTO(Long userId, String firstName, String secondName, AttendanceStatus status, AttendanceType attendanceType, String designation, String departmentName, Instant latestCheckInDate, Instant latestCheckOutDate) {
        this.userId = userId;
        this.fullName = firstName + " " + secondName;
        this.status = status;
        this.attendanceType = attendanceType;
        this.designation = designation;
        this.departmentName = departmentName;
        if (latestCheckInDate != null) {
            this.firstCheckInTime = latestCheckInDate.toEpochMilli();
        } else {
            this.firstCheckInTime = 0L;
        }
        if (latestCheckOutDate != null) {
            this.lastCheckOutTime = latestCheckOutDate.toEpochMilli();
        } else {
            this.lastCheckOutTime = 0L;
        }
    }
}
