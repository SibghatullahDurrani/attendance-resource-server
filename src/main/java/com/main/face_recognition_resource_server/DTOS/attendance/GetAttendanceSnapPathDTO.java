package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetAttendanceSnapPathDTO {
  private String snapPath;
  private Long attendanceTime;

  GetAttendanceSnapPathDTO(String snapPath, Date date) {
    this.snapPath = snapPath;
    this.attendanceTime = date.getTime();
  }
}
