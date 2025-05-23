package com.main.face_recognition_resource_server.services.leave;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveDataWithApplicationDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveRequestDTO;
import com.main.face_recognition_resource_server.DTOS.leave.OrganizationLeaveRecordDTO;
import com.main.face_recognition_resource_server.constants.LeaveStatus;
import com.main.face_recognition_resource_server.constants.LeaveType;
import com.main.face_recognition_resource_server.exceptions.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface LeaveServices {
  void requestLeave(LeaveRequestDTO leaveRequest, String username) throws UserDoesntExistException, NoMoreLeavesRemainingException;

  List<LeaveDTO> getUserLeaves(String username, int year, int month) throws NoLeaveAvailableException;

  Page<OrganizationLeaveRecordDTO> getOrganizationLeavesPage(Long organizationId, int year, Integer month, String username, Long departmentName, LeaveType leaveType, LeaveStatus leaveStatus, PageRequest pageRequest);

  void doesLeaveBelongToOrganization(Long organizationId, Long leaveId) throws LeaveDoesntBelongToTheOrganizationException;

  String getLeaveApplication(Long leaveId) throws LeaveDoesntExistException;

  LeaveDataWithApplicationDTO getLeaveDataWithApplication(Long leaveId) throws LeaveDoesntExistException;
}
