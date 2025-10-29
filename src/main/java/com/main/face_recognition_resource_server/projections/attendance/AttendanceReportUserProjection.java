package com.main.face_recognition_resource_server.projections.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceReportUserProjection {
    private Long userId;
    private String firstName;
    private String secondName;
    private String divisionName;
    private String designation;
}
