package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceStatsDTO {
  private AttendanceCountDTO attendanceCount;
  private String averageCheckInTime;
  private String averageCheckOutTime;
}
