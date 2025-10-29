package com.main.face_recognition_resource_server.DTOS.export;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PreDataAttendanceDTO {
    private Long date;
    private Long checkIn;
    private Long checkOut;
    private AttendanceStatus attendanceStatus;
    private Long attendanceId;

    static PreDataAttendanceDTO from(ExcelAttendanceDTO attendanceDTO) {
        return PreDataAttendanceDTO.builder()
                .date(attendanceDTO.getDate().toEpochMilli())
                .checkIn(attendanceDTO.getCheckIn() != null ? attendanceDTO.getCheckIn().toEpochMilli() : null)
                .checkOut(attendanceDTO.getCheckOut() != null ? attendanceDTO.getCheckOut().toEpochMilli() : null)
                .attendanceStatus(attendanceDTO.getAttendanceStatus())
                .attendanceId(attendanceDTO.getAttendanceId())
                .build();
    }
}
