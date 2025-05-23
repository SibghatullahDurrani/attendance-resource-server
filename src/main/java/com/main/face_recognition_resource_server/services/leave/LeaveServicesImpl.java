package com.main.face_recognition_resource_server.services.leave;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveDataWithApplicationDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveRequestDTO;
import com.main.face_recognition_resource_server.DTOS.leave.OrganizationLeaveRecordDTO;
import com.main.face_recognition_resource_server.constants.LeaveStatus;
import com.main.face_recognition_resource_server.constants.LeaveType;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Leave;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.*;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import com.main.face_recognition_resource_server.repositories.leave.LeaveRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.main.face_recognition_resource_server.helpers.DateUtils.getStartAndEndDateOfMonthOfYear;
import static com.main.face_recognition_resource_server.helpers.DateUtils.getStartAndEndDateOfYear;

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

  @Override
  public Page<OrganizationLeaveRecordDTO> getOrganizationLeavesPage(Long organizationId, int year, Integer month, String userName, Long departmentId, LeaveType leaveType, LeaveStatus leaveStatus, PageRequest pageRequest) {
    Specification<Leave> specification = (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      Join<Leave, User> leaveUserJoin = root.join("user", JoinType.INNER);
      Join<User, Department> leaveUserDepartmentJoin = leaveUserJoin.join("department", JoinType.INNER);
      Join<Department, Organization> leaveUserDepartmentOrganizationJoin = leaveUserDepartmentJoin.join("organization", JoinType.INNER);

      predicates.add(criteriaBuilder.equal(leaveUserDepartmentOrganizationJoin.get("id"), organizationId));

      Date[] dates;
      if (month != null) {
        dates = getStartAndEndDateOfMonthOfYear(year, month);
      } else {
        dates = getStartAndEndDateOfYear(year);
      }
      predicates.add(criteriaBuilder.between(root.get("date"), dates[0].getTime(), dates[1].getTime()));
      if (userName != null) {
        predicates.add(criteriaBuilder.or(
                criteriaBuilder.like(leaveUserJoin.get("firstName"), "%" + userName + "%"),
                criteriaBuilder.like(leaveUserJoin.get("secondName"), "%" + userName + "%")
        ));
      }
      if (departmentId != null) {
        predicates.add(criteriaBuilder.equal(leaveUserDepartmentJoin.get("id"), departmentId));
      }
      if (leaveType != null) {
        predicates.add(criteriaBuilder.equal(root.get("type"), leaveType));
      }
      if (leaveStatus != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), leaveStatus));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
    return leaveRepository.getOrganizationLeaves(specification, pageRequest);
  }

  @Override
  public void doesLeaveBelongToOrganization(Long organizationId, Long leaveId) throws LeaveDoesntBelongToTheOrganizationException {
    Long organizationIdOfLeave = leaveRepository.getOrganizationIdOfLeave(leaveId);
    if (!organizationIdOfLeave.equals(organizationId)) {
      throw new LeaveDoesntBelongToTheOrganizationException();
    }
  }

  @Override
  public String getLeaveApplication(Long leaveId) throws LeaveDoesntExistException {
    Optional<String> leaveApplication = leaveRepository.getLeaveApplication(leaveId);
    if (leaveApplication.isEmpty()) {
      throw new LeaveDoesntExistException();
    } else {
      return leaveApplication.get();
    }
  }

  @Override
  public LeaveDataWithApplicationDTO getLeaveDataWithApplication(Long leaveId) throws LeaveDoesntExistException {
    Optional<LeaveDataWithApplicationDTO> leave = leaveRepository.getLeaveDataWithApplication(leaveId);
    if (leave.isEmpty()) {
      throw new LeaveDoesntExistException();
    }
    return leave.get();
  }
}
