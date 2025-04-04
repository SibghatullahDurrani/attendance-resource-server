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
  private int daysPresent;
  private int daysAbsent;
  private int daysOnLeave;
  private int daysLate;
  private String averageCheckInTime;
  private String averageCheckOutTime;
}
