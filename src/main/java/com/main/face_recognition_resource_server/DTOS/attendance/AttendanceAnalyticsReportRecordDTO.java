package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceAnalyticsReportRecordDTO {
    private String firstName;
    private String secondName;
    private String division;
    private String designation;
    private Map<Long, AttendanceStatus> attendances;

    AttendanceAnalyticsReportRecordDTO(String firstName, String secondName, String division, String designation) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.division = division;
        this.designation = designation;
    }
}
