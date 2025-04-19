package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AttendanceGraphDataDTO {
  private Long date;
  private Long presentCount;
  private Long lateCount;
  private Long absentCount;
  private Long leaveCount;

  public AttendanceGraphDataDTO(Date date, Long presentCount, Long lateCount, Long absentCount, Long leaveCount) {
    this.date = date.getTime();
    this.presentCount = presentCount;
    this.lateCount = lateCount;
    this.absentCount = absentCount;
    this.leaveCount = leaveCount;
  }
}
