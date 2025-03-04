package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.CheckInDTO;
import com.main.face_recognition_resource_server.DTOS.CheckOutDTO;
import com.main.face_recognition_resource_server.DTOS.UserAttendanceDTO;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.AttendanceDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.AttendanceRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceServicesImpl implements AttendanceServices {
  private final AttendanceRepository attendanceRepository;
  private final UserServices userServices;
  private final CheckInServices checkInServices;
  private final CheckOutServices checkOutServices;
  private final OrganizationServices organizationServices;


  public AttendanceServicesImpl(AttendanceRepository attendanceRepository, UserServices userServices, CheckInServices checkInServices, CheckOutServices checkOutServices, OrganizationServices organizationServices) {
    this.attendanceRepository = attendanceRepository;
    this.userServices = userServices;
    this.checkInServices = checkInServices;
    this.checkOutServices = checkOutServices;
    this.organizationServices = organizationServices;
  }

  @Override
  @Transactional
  @Async
  public void markCheckIn(Long userId, Date checkInDate, BufferedImage image) throws UserDoesntExistException, IOException {
    Optional<Attendance> attendance = getUserAttendanceFromDayStartTillDate(userId, checkInDate);
    Long organizationId = this.userServices.getUserOrganizationIdByUserId(userId);

    if (attendance.isEmpty()) {
      User user = userServices.getUserById(userId);

      String checkInPolicyTime = this.organizationServices.getOrganizationCheckInPolicy(organizationId);
      int lateAttendanceToleranceTimePolicy = this.organizationServices.getOrganizationLateAttendanceToleranceTimePolicy(organizationId);

      int lateAttendanceToleranceTimeHours = 0;
      int lateAttendanceToleranceTimeMinutes = 0;
      while (lateAttendanceToleranceTimePolicy >= 60) {
        lateAttendanceToleranceTimeHours += 1;
        lateAttendanceToleranceTimePolicy -= 60;
      }
      lateAttendanceToleranceTimeMinutes += lateAttendanceToleranceTimePolicy;


      String[] timeSplit = checkInPolicyTime.split(":");
      Calendar requiredCheckInTime = GregorianCalendar.getInstance();
      requiredCheckInTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]) + lateAttendanceToleranceTimeHours);
      requiredCheckInTime.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]) + lateAttendanceToleranceTimeMinutes);

      Calendar checkedInTime = GregorianCalendar.getInstance();
      checkedInTime.setTime(checkInDate);
      AttendanceStatus attendanceStatus;
      if (checkedInTime.after(requiredCheckInTime)) {
        attendanceStatus = AttendanceStatus.LATE;
      } else {
        attendanceStatus = AttendanceStatus.ON_TIME;
      }

      Attendance attendanceAdded = attendanceRepository.saveAndFlush(
              Attendance.builder()
                      .user(user)
                      .date(checkInDate)
                      .status(attendanceStatus)
                      .build()
      );
      checkInServices.saveCheckIn(checkInDate, attendanceAdded, image);
    } else {
      checkInServices.saveCheckIn(checkInDate, attendance.get(), image);
    }
  }

  @Override
  @Transactional
  @Async
  public void markCheckOut(Long userId, Date endDate, BufferedImage image) throws IOException {
    Optional<Attendance> attendance = getUserAttendanceFromDayStartTillDate(userId, endDate);
    if (attendance.isPresent()) {
      checkOutServices.saveCheckOut(endDate, attendance.get(), image);
    }
  }

  @Override
  public UserAttendanceDTO getAttendanceOfUserOnDate(Long userId, String stringDate) throws UserDoesntExistException, AttendanceDoesntExistException {
    boolean userExists = userServices.userExistsWithUserId(userId);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String stringStartDate = stringDate + " 00:00:00";
    String stringEndDate = stringDate + " 23:59:59";
    LocalDateTime startLocalDateTime = LocalDateTime.parse(stringStartDate, dateTimeFormatter);
    LocalDateTime endLocalDateTime = LocalDateTime.parse(stringEndDate, dateTimeFormatter);
    Date startDate = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
    if (userExists) {
      Optional<UserAttendanceDTO> attendance = attendanceRepository.getAttendanceDTOByUserIdAndDate(userId, startDate, endDate);
      if (attendance.isEmpty()) {
        throw new AttendanceDoesntExistException();
      } else {
        List<CheckInDTO> checkIns = checkInServices.getCheckInsByAttendanceId(attendance.get().getId());
        List<CheckOutDTO> checkOuts = checkOutServices.getCheckOutsByAttendanceId(attendance.get().getId());
        attendance.get().setCheckIns(checkIns);
        attendance.get().setCheckOuts(checkOuts);
        return attendance.get();
      }
    }
    return null;
  }

  @Override
  public void markAbsentOfAllUsersInOrganizationForCurrentDay(Long organizationId) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);

    boolean exists = this.attendanceRepository.existsByDateAndOrganizationId(calendar.getTime(), organizationId);
    if (!exists) {

    }
  }

  private Optional<Attendance> getUserAttendanceFromDayStartTillDate(Long userId, Date endDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(endDate);
    Date startDate = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime();
    return attendanceRepository.getAttendanceByUserIdAndDate(userId, startDate, endDate);
  }

  @Override
  public Set<Long> getCache(Long organizationId, CameraType type) {
    Date endDate = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(endDate);
    Date startDate = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime();
    List<Attendance> attendances = attendanceRepository.getAttendanceOfOrganizationBetweenTime(startDate, endDate, organizationId);
    Set<Long> userSet = new TreeSet<>();
    if (type == CameraType.IN) {
      for (Attendance attendance : attendances) {
        if (!attendance.getCheckOuts().isEmpty()) {
          Date maxCheckOut = maxCheckOut(attendance.getCheckOuts());
          Date maxCheckIn = maxCheckIn(attendance.getCheckIns());
          if (maxCheckIn.after(maxCheckOut)) {
            userSet.add(attendance.getUser().getId());
          }
        } else {
          userSet.add(attendance.getUser().getId());
        }
      }
    } else {
      for (Attendance attendance : attendances) {
        if (!attendance.getCheckOuts().isEmpty()) {
          Date maxCheckOut = maxCheckOut(attendance.getCheckOuts());
          Date maxCheckIn = maxCheckIn(attendance.getCheckIns());
          if (maxCheckOut.after(maxCheckIn)) {
            userSet.add(attendance.getUser().getId());
          }
        }
      }
    }
    return userSet;
  }

  private Date maxCheckOut(List<CheckOut> checkOuts) {
    Date maxCheckOut = checkOuts.getFirst().getDate();
    for (CheckOut checkOut : checkOuts) {
      if (checkOut.getDate().after(maxCheckOut)) {
        maxCheckOut = checkOut.getDate();
      }
    }
    return maxCheckOut;
  }

  private Date maxCheckIn(List<CheckIn> checkIns) {
    Date maxCheckIn = checkIns.getFirst().getDate();
    for (CheckIn checkIn : checkIns) {
      if (checkIn.getDate().after(maxCheckIn)) {
        maxCheckIn = checkIn.getDate();
      }
    }
    return maxCheckIn;
  }
}
