package com.main.face_recognition_resource_server.DTOS.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RegisterOrganizationPoliciesDTO {
  private String checkInTimeForUser;
  private String checkOutTimeForUser;
  private int lateAttendanceToleranceTimeInMinutes;
  private int retakeAttendanceInHour;
  private int checkOutToleranceTimeInHour;
  private int sickLeavesAllowed;
  private int annualLeavesAllowed;
}
