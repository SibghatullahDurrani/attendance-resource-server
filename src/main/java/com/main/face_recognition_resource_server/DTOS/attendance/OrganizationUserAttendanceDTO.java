package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrganizationUserAttendanceDTO {
  private Long id;
  private String fullName;
  private String departmentName;
  private String designation;
  private Long daysPresent;
  private Long daysOnTime;
  private Long daysAbsent;
  private Long daysOnLeave;
  private Long daysLate;

  OrganizationUserAttendanceDTO(Long id, String firstName, String secondName, String departmentName, String designation, Long daysPresent, Long daysOnTime, Long daysAbsent, Long daysOnLeave, Long daysLate) {
    this.id = id;
    this.fullName = firstName + " " + secondName;
    this.departmentName = departmentName;
    this.designation = designation;
    this.daysPresent = daysPresent;
    this.daysOnTime = daysOnTime;
    this.daysAbsent = daysAbsent;
    this.daysOnLeave = daysOnLeave;
    this.daysLate = daysLate;
  }
}
