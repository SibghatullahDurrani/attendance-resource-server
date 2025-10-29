package com.main.face_recognition_resource_server.DTOS.leave;

import com.main.face_recognition_resource_server.constants.leave.LeaveStatus;
import com.main.face_recognition_resource_server.constants.leave.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaveApplicationWithUserDataDTO {
    private OrganizationUserLeaveRecordDTO data;
    private UserLeaveApplicationDTO application;

    LeaveApplicationWithUserDataDTO(Long id, Date date, LeaveStatus leaveStatus, LeaveType leaveType, String firstName, String secondName, String departmentName, String application) {
        this.data = new OrganizationUserLeaveRecordDTO(id, date, leaveStatus, leaveType, firstName, secondName, departmentName);
        this.application = new UserLeaveApplicationDTO(application);
    }
}
