package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DailyUserAttendanceDTO {
  private Long userId;
  private String fullName;
  private AttendanceStatus status;
  private AttendanceType attendanceType;
  private String departmentName;

  public DailyUserAttendanceDTO(Long userId, String firstName, String secondName, AttendanceStatus status, AttendanceType attendanceType, String departmentName) {
    this.userId = userId;
    this.fullName = firstName + " " + secondName;
    this.status = status;
    this.attendanceType = attendanceType;
    this.departmentName = departmentName;
  }
}
