package com.main.face_recognition_resource_server.DTOS.leave;

import com.main.face_recognition_resource_server.constants.LeaveStatus;
import com.main.face_recognition_resource_server.constants.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaveDataWithApplicationDTO {
  private OrganizationLeaveRecordDTO data;
  private LeaveApplicationDTO application;

  LeaveDataWithApplicationDTO(Long id, Date date, LeaveStatus leaveStatus, LeaveType leaveType, String firstName, String secondName, String departmentName, String application) {
    this.data = new OrganizationLeaveRecordDTO(id, date, leaveStatus, leaveType, firstName, secondName, departmentName);
    this.application = new LeaveApplicationDTO(application);
  }
}
