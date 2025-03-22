package com.main.face_recognition_resource_server.DTOS.leave;

import com.main.face_recognition_resource_server.constants.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LeaveRequestDTO {
  private String leaveApplication;
  private int month;
  private int year;
  private int date;
  private LeaveType leaveType;
}
