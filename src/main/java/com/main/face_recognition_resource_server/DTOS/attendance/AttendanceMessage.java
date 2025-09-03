package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class AttendanceMessage {
    private Long id;
    private Long userId;
    private Long date;
    private AttendanceType attendanceType;
}
