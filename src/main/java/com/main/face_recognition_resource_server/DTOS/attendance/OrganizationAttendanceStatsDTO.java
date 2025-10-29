package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrganizationAttendanceStatsDTO {
    private Long totalUsers;
    private Long usersPresent;
    private Long usersOnTime;
    private Long usersAbsent;
    private Long usersOnLeave;
    private Long usersLate;
}