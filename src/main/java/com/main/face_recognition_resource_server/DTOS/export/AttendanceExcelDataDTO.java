package com.main.face_recognition_resource_server.DTOS.export;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class AttendanceExcelDataDTO {
    private Long userId;
    private String firstName;
    private String secondName;
    private String division;
    private String designation;
    private List<ExcelAttendanceDTO> attendances;

    AttendanceExcelDataDTO(Long userId, String firstName, String secondName, String division, String designation) {
        this.userId = userId;
        this.firstName = firstName;
        this.secondName = secondName;
        this.division = division;
        this.designation = designation;
    }
}
