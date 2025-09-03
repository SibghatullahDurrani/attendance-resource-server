package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceLiveFeedDTO {
    private long userId;
    private String fullName;
    private String designation;
    private String departmentName;
    private AttendanceType attendanceType;
    private AttendanceStatus attendanceStatus;
    private long checkInTime;
    private long checkOutTime;
    private byte[] sourceImage;
}
