package com.main.face_recognition_resource_server.projections.attendance;

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
public class CheckInCheckOutReportAttendanceProjection {
    private Long userId;
    private Long attendanceId;
    private Instant date;
    private AttendanceStatus attendanceStatus;
    private Instant firstCheckIn;
    private Instant lastCheckOut;
}