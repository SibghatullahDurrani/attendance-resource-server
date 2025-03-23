package com.main.face_recognition_resource_server.services.leave;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveRequestDTO;
import com.main.face_recognition_resource_server.constants.LeaveStatus;
import com.main.face_recognition_resource_server.constants.LeaveType;
import com.main.face_recognition_resource_server.domains.Leave;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.NoLeaveAvailableException;
import com.main.face_recognition_resource_server.exceptions.NoMoreLeavesRemainingException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.LeaveRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.main.face_recognition_resource_server.helpers.DateUtils.getStartAndEndDateOfMonthOfYear;

@Service
public class LeaveServicesImpl implements LeaveServices {
  private final LeaveRepository leaveRepository;
  private final UserServices userServices;


  public LeaveServicesImpl(LeaveRepository leaveRepository, UserServices userServices, UserRepository userRepository) {
    this.leaveRepository = leaveRepository;
    this.userServices = userServices;
  }

  @Override
  @Transactional
  public void requestLeave(LeaveRequestDTO leaveRequest, String username) throws UserDoesntExistException, NoMoreLeavesRemainingException {
    User user = userServices.getUserByUsername(username);
    if (leaveRequest.getLeaveType() == LeaveType.SICK_LEAVE && user.getRemainingSickLeaves() == 0)
      throw new NoMoreLeavesRemainingException();
    else if (leaveRequest.getLeaveType() == LeaveType.ANNUAL_LEAVE && user.getRemainingAnnualLeaves() == 0)
      throw new NoMoreLeavesRemainingException();
    else if (leaveRequest.getLeaveType() == LeaveType.ANNUAL_LEAVE)
      user.setRemainingAnnualLeaves(user.getRemainingAnnualLeaves() - 1);
    else
      user.setRemainingSickLeaves(user.getRemainingSickLeaves() - 1);


    userServices.saveUser(user);

    Leave leave = Leave.builder()
            .date(new GregorianCalendar(leaveRequest.getYear(), leaveRequest.getMonth(), leaveRequest.getDate()).getTime())
            .leaveApplication(leaveRequest.getLeaveApplication())
            .type(leaveRequest.getLeaveType())
            .status(LeaveStatus.PENDING)
            .user(user)
            .build();

    leaveRepository.saveAndFlush(leave);
  }

  @Override
  public List<LeaveDTO> getUserLeaves(String username, int year, int month) throws NoLeaveAvailableException {
    Date[] startAndEndDate = getStartAndEndDateOfMonthOfYear(year, month);
    List<LeaveDTO> userLeaves = leaveRepository.getUserLeavesBetweenDates(startAndEndDate[0], startAndEndDate[1], username);
    if (userLeaves.isEmpty()) {
      throw new NoLeaveAvailableException();
    } else {
      return userLeaves;
    }
  }
}
