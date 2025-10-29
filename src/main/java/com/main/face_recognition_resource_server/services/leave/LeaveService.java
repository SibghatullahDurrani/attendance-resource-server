package com.main.face_recognition_resource_server.services.leave;

import com.main.face_recognition_resource_server.DTOS.leave.*;
import com.main.face_recognition_resource_server.constants.leave.LeaveStatus;
import com.main.face_recognition_resource_server.constants.leave.LeaveType;
import com.main.face_recognition_resource_server.exceptions.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface LeaveService {
    void requestLeave(LeaveRequestDTO leaveRequest, String username) throws UserDoesntExistException, NoMoreLeavesRemainingException;

    List<UserLeaveDTO> getUserLeaves(String username, int year, int month) throws NoLeaveAvailableException;

    Page<OrganizationUserLeaveRecordDTO> getOrganizationUserLeaves(Long organizationId, int year, Integer month, String username, Long departmentName, LeaveType leaveType, LeaveStatus leaveStatus, PageRequest pageRequest);

    void doesLeaveBelongToOrganization(Long organizationId, Long leaveId) throws LeaveDoesntBelongToTheOrganizationException;

    String getUserLeaveApplication(Long leaveId) throws LeaveDoesntExistException;

    LeaveApplicationWithUserDataDTO getLeaveApplicationWithUserData(Long leaveId) throws LeaveDoesntExistException;

    void respondToLeave(RespondToLeaveDTO respondToLeaveDTO, Long organizationId);
}
