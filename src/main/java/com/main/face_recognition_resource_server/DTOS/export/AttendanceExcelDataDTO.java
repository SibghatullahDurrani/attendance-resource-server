package com.main.face_recognition_resource_server.DTOS.export;

import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class AttendanceExcelDataDTO {
    private String firstName;
    private String secondName;
    private String division;
    private String designation;
    private Date date;
    private Date checkIn;
    private Date checkOut;
    private AttendanceStatus attendanceStatus;
    private Long attendanceId;

    AttendanceExcelDataDTO(String firstName, String secondName, String division, String designation, Date date, AttendanceStatus attendanceStatus, Long attendanceId) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.division = division;
        this.designation = designation;
        this.date = date;
        this.attendanceStatus = attendanceStatus;
        this.attendanceId = attendanceId;
    }
}
