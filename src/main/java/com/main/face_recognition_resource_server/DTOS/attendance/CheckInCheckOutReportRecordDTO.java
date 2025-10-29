package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CheckInCheckOutReportRecordDTO {
    private String firstName;
    private String secondName;
    private String division;
    private String designation;
    private List<CheckInCheckOutReportAttendance> reportAttendances;

    CheckInCheckOutReportRecordDTO(String firstName, String secondName, String division, String designation) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.division = division;
        this.designation = designation;
    }
}
