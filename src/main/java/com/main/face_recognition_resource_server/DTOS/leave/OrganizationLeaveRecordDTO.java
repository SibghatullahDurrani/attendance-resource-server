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
public class OrganizationLeaveRecordDTO {
    private Long id;
    private Long date;
    private LeaveStatus leaveStatus;
    private LeaveType leaveType;
    private String fullName;
    private String departmentName;

    OrganizationLeaveRecordDTO(Long id, Date date, LeaveStatus leaveStatus, LeaveType leaveType, String firstName, String secondName, String departmentName) {
        this.id = id;
        this.date = date.getTime();
        this.leaveStatus = leaveStatus;
        this.leaveType = leaveType;
        this.fullName = firstName + " " + secondName;
        this.departmentName = departmentName;
    }
}
