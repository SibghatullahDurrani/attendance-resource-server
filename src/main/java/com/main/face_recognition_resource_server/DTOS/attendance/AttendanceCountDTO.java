package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AttendanceCountDTO {
  private long daysPresent;
  private long daysAbsent;
  private long daysOnLeave;
  private long daysLate;
}
