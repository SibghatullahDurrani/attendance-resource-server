package com.main.face_recognition_resource_server.services.leave;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveRequestDTO;
import com.main.face_recognition_resource_server.exceptions.NoLeaveAvailableException;
import com.main.face_recognition_resource_server.exceptions.NoMoreLeavesRemainingException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;

import java.util.List;

public interface LeaveServices {
  void requestLeave(LeaveRequestDTO leaveRequest, String username) throws UserDoesntExistException, NoMoreLeavesRemainingException;

  List<LeaveDTO> getUserLeaves(String username, int year, int month) throws NoLeaveAvailableException;
}
