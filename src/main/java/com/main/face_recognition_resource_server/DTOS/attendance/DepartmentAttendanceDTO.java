package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DepartmentAttendanceDTO {
  private String departmentName;
  private Long onTime;
  private Long late;
  private Long absent;
  private Long onLeave;
  private Long total;

  public DepartmentAttendanceDTO(Long onTime, Long late, Long absent, Long onLeave) {
    this.onTime = onTime;
    this.late = late;
    this.absent = absent;
    this.onLeave = onLeave;
  }
}
