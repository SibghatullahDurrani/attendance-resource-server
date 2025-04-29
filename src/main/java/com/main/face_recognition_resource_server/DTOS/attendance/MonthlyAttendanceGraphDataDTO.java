package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonthlyAttendanceGraphDataDTO {
  private int month;
  private Long presentCount;
  private Long lateCount;
  private Long absentCount;
  private Long leaveCount;
}
