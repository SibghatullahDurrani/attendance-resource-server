package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.*;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.AttendanceDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.AttendanceRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormatSymbols;
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
    Optional<Attendance> attendanceOptional = getUserAttendanceFromDayStartTillDate(userId, checkInDate);
    Long organizationId = this.userServices.getUserOrganizationIdByUserId(userId);

    if (attendanceOptional.isPresent()) {
      Attendance attendance = attendanceOptional.get();
      if (attendance.getCheckIns() == null || attendance.getCheckIns().isEmpty()) {
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

        attendance.setStatus(attendanceStatus);
        attendanceRepository.saveAndFlush(attendance);

        checkInServices.saveCheckIn(checkInDate, attendance, image);

      } else {
        checkInServices.saveCheckIn(checkInDate, attendance, image);
      }
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
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    boolean exists = this.attendanceRepository.existsByDateAndOrganizationId(calendar.getTime(), organizationId);
    if (!exists) {
      List<User> users = userServices.getUsersByOrganizationId(organizationId);
      List<Attendance> attendances = new ArrayList<>();
      for (User user : users) {
        attendances.add(Attendance.builder().user(user).date(calendar.getTime()).status(AttendanceStatus.ABSENT).build());
      }
      attendanceRepository.saveAllAndFlush(attendances);
    }
  }

  @Override
  public AttendanceStatsDTO getUserAttendanceStats(int year, Long userId) throws NoStatsAvailableException {
    Date startDate = new GregorianCalendar(year, Calendar.JANUARY, 1).getTime();
    Calendar endCalendar = GregorianCalendar.getInstance();
    endCalendar.set(Calendar.YEAR, year);
    endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
    endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    endCalendar.set(Calendar.HOUR_OF_DAY, 0);
    Date endDate = endCalendar.getTime();
    return generateAttendanceStatsDTO(startDate, endDate, userId);
  }

  @Override
  public AttendanceStatsDTO getUserAttendanceStats(int month, int year, Long userId) throws NoStatsAvailableException {
    Date[] startAndEndDate = getStartAndEndDateOfMonthOfYear(month, year);
    return generateAttendanceStatsDTO(startAndEndDate[0], startAndEndDate[1], userId);
  }

  private Date[] getStartAndEndDateOfMonthOfYear(int month, int year) {
    Date startDate = new GregorianCalendar(year, month, 1, 0, 0).getTime();
    Calendar endCalendar = GregorianCalendar.getInstance();
    endCalendar.set(Calendar.YEAR, year);
    endCalendar.set(Calendar.MONTH, month);
    endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
    endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
    endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
    Date endDate = endCalendar.getTime();
    return new Date[]{startDate, endDate};

  }

  @Override
  public AttendanceCalendarDTO getUserAttendanceCalendar(int month, int year, Long userId) throws NoStatsAvailableException {
    Date[] startAndEndDate = getStartAndEndDateOfMonthOfYear(month, year);
    List<CalendarAttendanceDataDTO> data = attendanceRepository.getAttendanceStatusWithDateOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);
    if (data.isEmpty()) {
      throw new NoStatsAvailableException();
    }
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(startAndEndDate[0]);
    DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
    String firstDayOfMonth = symbols.getWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];
    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return AttendanceCalendarDTO.builder()
            .data(data)
            .maxDays(maxDays)
            .firstDayOfTheMonth(firstDayOfMonth)
            .build();
  }

  private AttendanceStatsDTO generateAttendanceStatsDTO(Date startDate, Date endDate, Long userId) throws NoStatsAvailableException {
    int presentCount = attendanceRepository.countPresentAttendancesOfUserBetweenDates(startDate, endDate, userId, AttendanceStatus.ON_TIME, AttendanceStatus.LATE);
    int absentCount = attendanceRepository.countAbsentAttendancesOfUserBetweenDates(startDate, endDate, userId, AttendanceStatus.ABSENT);
    int leaveCount = attendanceRepository.countLeaveAttendancesOfUserBetweenDates(startDate, endDate, userId, AttendanceStatus.ON_LEAVE);
    List<Long> attendanceIds = attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, userId);
    String averageCheckIns = checkInServices.getAverageCheckInOfAttendances(attendanceIds);
    String averageCheckOuts = checkOutServices.getAverageCheckOutOfAttendances(attendanceIds);
    return new AttendanceStatsDTO(presentCount, absentCount, leaveCount, averageCheckIns, averageCheckOuts);
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
    List<Attendance> attendances = attendanceRepository.getPresentAttendanceOfOrganizationBetweenTime(startDate, endDate, organizationId, AttendanceStatus.ON_TIME, AttendanceStatus.LATE);
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
