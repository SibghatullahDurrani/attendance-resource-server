package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.AttendanceRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.List;
import java.util.*;

@Service
public class AttendanceServicesImpl implements AttendanceServices {
  private final AttendanceRepository attendanceRepository;
  private final UserServices userServices;
  private final CheckInServices checkInServices;
  private final CheckOutServices checkOutServices;
  private final OrganizationServices organizationServices;
  private final int scoreImageWidth = 1200;
  private final int scoreImageHeight = 100;
  private final Color green = new Color(67, 99, 63);
  private final Color red = new Color(163, 0, 0);
  private final Color cyan = new Color(209, 232, 111);


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
        String checkInPolicyTime = organizationServices.getOrganizationCheckInPolicy(organizationId);
        int lateAttendanceToleranceTimePolicy = organizationServices.getOrganizationLateAttendanceToleranceTimePolicy(organizationId);

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
    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
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
    calendar.set(Calendar.DAY_OF_MONTH, maxDays);
    String lastDayOfMonth = symbols.getWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    int previousMonth = calendar.get(Calendar.MONTH) - 1;
    calendar.set(Calendar.MONTH, previousMonth);
    int lastDateOfPreviousMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return AttendanceCalendarDTO.builder().data(data).maxDays(maxDays).firstDayOfTheMonth(firstDayOfMonth).lastDayOfTheMonth(lastDayOfMonth).lastDateOfPreviousMonth(lastDateOfPreviousMonth).build();
  }

  @Override
  public List<UserAttendanceTableDTO> getMonthlyUserAttendanceTable(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException, IOException {
    Date[] startAndEndDate = getStartAndEndDateOfMonthOfYear(month, year);
    List<UserAttendanceTableDTO> attendanceTableRecords = attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);

    if (attendanceTableRecords.isEmpty()) {
      throw new NoStatsAvailableException();
    }

    Long organizationId = userServices.getUserOrganizationIdByUserId(userId);
    int lateAttendanceToleranceTimeInMillis = organizationServices.getOrganizationLateAttendanceToleranceTimePolicy(organizationId) * 60000;
    long organizationRetakeAttendancePolicyInMillis = organizationServices.getAttendanceRetakeAttendanceInHourPolicy(organizationId) * 3600000L;
    String organizationCheckInTime = organizationServices.getOrganizationCheckInPolicy(organizationId);
    String[] organizationCheckInTimeSplit = organizationCheckInTime.split(":");
    int organizationCheckInHour = Integer.parseInt(organizationCheckInTimeSplit[0]);
    int organizationCheckInMinutes = Integer.parseInt(organizationCheckInTimeSplit[1]);
    String organizationCheckOutTime = organizationServices.getOrganizationCheckOutPolicy(organizationId);
    String[] organizationCheckOutTimeSplit = organizationCheckOutTime.split(":");
    int organizationCheckOutHour = Integer.parseInt(organizationCheckOutTimeSplit[0]);
    int organizationCheckOutMinutes = Integer.parseInt(organizationCheckOutTimeSplit[1]);
    int checkOutToleranceTime = organizationServices.getOrganizationCheckOutToleranceTimePolicy(organizationId);

    for (UserAttendanceTableDTO attendanceRecord : attendanceTableRecords) {
      attendanceRecord.setCheckIns(checkInServices.getCheckInTimesByAttendanceId(attendanceRecord.getId()));
      attendanceRecord.setCheckOuts(checkOutServices.getCheckOutTimesByAttendanceId(attendanceRecord.getId()));

      BufferedImage scoreImage = new BufferedImage(scoreImageWidth, scoreImageHeight, BufferedImage.TYPE_INT_RGB);
      Graphics scoreGraphics = scoreImage.createGraphics();

      Calendar policyCheckIn = new GregorianCalendar();
      Calendar policyCheckOut = new GregorianCalendar();

      policyCheckIn.setTimeInMillis(attendanceRecord.getDate());
      policyCheckOut.setTimeInMillis(attendanceRecord.getDate());

      policyCheckIn.set(Calendar.HOUR_OF_DAY, organizationCheckInHour);
      policyCheckIn.set(Calendar.MINUTE, organizationCheckInMinutes);

      policyCheckOut.set(Calendar.HOUR_OF_DAY, organizationCheckOutHour);
      policyCheckOut.set(Calendar.MINUTE, organizationCheckOutMinutes);

      long policyCheckOutTimeStamp = policyCheckOut.getTime().getTime();
      long policyCheckInTimeStamp = policyCheckIn.getTime().getTime();

      long totalWorkingHoursTimeStamp = policyCheckOutTimeStamp - policyCheckInTimeStamp;

      List<Long> checkIns = new ArrayList<>(attendanceRecord.getCheckIns());
      List<Long> checkOuts = new ArrayList<>(attendanceRecord.getCheckOuts());

      long checkIn = 0;
      long checkOut = 0;
      if (!checkIns.isEmpty()) {
        checkIn = checkIns.removeFirst();
      }
      if (!checkOuts.isEmpty()) {
        checkOut = checkOuts.removeFirst();
      }
      int previousPoint = 0;
      boolean first = true;

      if (checkIn == 0 && attendanceRecord.getStatus() == AttendanceStatus.ABSENT) {
        drawRectangleTillEnd(scoreGraphics, previousPoint, red);
      } else if (checkIn == 0 && attendanceRecord.getStatus() == AttendanceStatus.ON_LEAVE) {
        drawRectangleTillEnd(scoreGraphics, previousPoint, cyan);
      }

      while (checkIn != 0 || checkOut != 0) {
        if (first) {
          if (checkIn > policyCheckInTimeStamp + lateAttendanceToleranceTimeInMillis) {
            previousPoint = drawRectangle(scoreGraphics, previousPoint, policyCheckInTimeStamp, checkIn, totalWorkingHoursTimeStamp, red);
            if (checkIns.isEmpty()) {
              if (checkOut == 0) {
                if (checkIn + organizationRetakeAttendancePolicyInMillis > policyCheckOutTimeStamp) {
                  drawRectangle(scoreGraphics, previousPoint, checkIn, policyCheckOutTimeStamp, totalWorkingHoursTimeStamp, green);
                } else {
                  int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkIn + organizationRetakeAttendancePolicyInMillis, totalWorkingHoursTimeStamp, green);
                  previousPoint = previousPoint + newPoint;
                  drawRectangleTillEnd(scoreGraphics, previousPoint, red);
                }
              } else {
                int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkOut, totalWorkingHoursTimeStamp, green);
                previousPoint = previousPoint + newPoint;
              }
              checkIn = 0;
            } else {
              checkIn = checkIns.removeFirst();
            }
          } else {
            int newPoint = drawRectangle(scoreGraphics, previousPoint, policyCheckInTimeStamp, checkIn, totalWorkingHoursTimeStamp, green);
            previousPoint = previousPoint + newPoint;
          }
          first = false;
        } else {
          if (checkIn != 0 && checkOut == 0) {
            if ((checkIn + organizationRetakeAttendancePolicyInMillis) > policyCheckOutTimeStamp) {
              drawRectangle(scoreGraphics, previousPoint, checkIn, policyCheckOutTimeStamp, totalWorkingHoursTimeStamp, green);
            } else {
              int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkIn + organizationRetakeAttendancePolicyInMillis, totalWorkingHoursTimeStamp, green);
              previousPoint = previousPoint + newPoint;
              drawRectangleTillEnd(scoreGraphics, previousPoint, red);
            }
            checkIn = 0;
          }
          if (checkIn == 0 && checkOut != 0) {
            drawRectangleTillEnd(scoreGraphics, previousPoint, red);
            checkOut = 0;
          }
          if (checkOut != 0) {
            if (checkOut > checkIn) {
              int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkOut, totalWorkingHoursTimeStamp, green);
              previousPoint = previousPoint + newPoint;
              if (checkIns.isEmpty()) {
                checkIn = 0;
              } else {
                checkIn = checkIns.removeFirst();
              }
            } else {
              int newPoint = drawRectangle(scoreGraphics, previousPoint, checkOut, checkIn, totalWorkingHoursTimeStamp, red);
              previousPoint = previousPoint + newPoint;
              if (checkOuts.isEmpty()) {
                checkOut = 0;
              } else {
                checkOut = checkOuts.removeFirst();
              }
            }
          }
        }
      }

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ImageIO.write(scoreImage, "jpg", byteArrayOutputStream);
      attendanceRecord.setScore(byteArrayOutputStream.toByteArray());
    }
    return attendanceTableRecords;
  }

  private int drawRectangle(Graphics scoreGraphics, int previousPoint, long startTime, long endTime, long totalTime, Color color) {
    double redPercentage = (double) (endTime - startTime) / totalTime;
    int width = (int) (scoreImageWidth * redPercentage);
    scoreGraphics.setColor(color);
    scoreGraphics.fillRect(previousPoint, 0, width, scoreImageHeight);
    return width;
  }

  private void drawRectangleTillEnd(Graphics scoreGraphics, int previousPoint, Color color) {
    scoreGraphics.setColor(color);
    scoreGraphics.fillRect(previousPoint, 0, scoreImageWidth - previousPoint, scoreImageHeight);
  }

  @Override
  public List<UserAttendanceDTO> getMonthlyUserAttendanceOverview(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException {
    Date[] startAndEndDate = getStartAndEndDateOfMonthOfYear(month, year);
    List<UserAttendanceDTO> attendances = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);

    if (attendances.isEmpty()) {
      throw new NoStatsAvailableException();
    }

    for (UserAttendanceDTO attendance : attendances) {
      attendance.setCheckIns(checkInServices.getCheckInTimesByAttendanceId(attendance.getId()));
      attendance.setCheckOuts(checkOutServices.getCheckOutTimesByAttendanceId(attendance.getId()));
    }

    return attendances;
  }

  @Override
  public AttendanceSnapshotDTO getUserAttendanceSnapshots(int year, int month, int day, Long userId) throws NoStatsAvailableException {
    Calendar startCalendar = new GregorianCalendar(year, month, day, 0, 0);
    Date startDate = startCalendar.getTime();
    Calendar endCalendar = new GregorianCalendar(year, month, day, 23, 59);
    Date endDate = endCalendar.getTime();

    Optional<Long> attendanceId = attendanceRepository.getAttendanceIdOfUserBetweenDates(startDate, endDate, userId);

    if (attendanceId.isEmpty()) {
      throw new NoStatsAvailableException();
    }

    AttendanceSnapshotDTO attendanceSnapshot = AttendanceSnapshotDTO.builder().attendanceStatus(attendanceRepository.getAttendanceStatusOfAttendance(attendanceId.get())).dayTime(startDate.getTime()).data(new ArrayList<>()).build();

    attendanceSnapshot.addAttendanceSnapshotDTOData(checkInServices.getCheckInSnapshotsOfAttendance(attendanceId.get()));
    attendanceSnapshot.addAttendanceSnapshotDTOData(checkOutServices.getCheckOutSnapshotsOfAttendance(attendanceId.get()));

    return attendanceSnapshot;
  }

  @Override
  public Page<UserAttendanceDTO> getYearlyUserAttendanceTable(Pageable pageRequest, int year, Long userId) {
    Date startDate = new GregorianCalendar(year, Calendar.JANUARY, 1).getTime();
    Calendar endCalendar = GregorianCalendar.getInstance();
    endCalendar.set(Calendar.YEAR, year);
    endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
    endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
    Date endDate = endCalendar.getTime();

    Page<UserAttendanceDTO> attendancePage = attendanceRepository.getUserAttendancePageBetweenDate(userId, startDate, endDate, pageRequest);

    return attendancePage.map(attendance -> {
      attendance.setCheckIns(checkInServices.getCheckInTimesByAttendanceId(attendance.getId()));
      attendance.setCheckOuts(checkOutServices.getCheckOutTimesByAttendanceId(attendance.getId()));
      return attendance;
    });
  }

  private AttendanceStatsDTO generateAttendanceStatsDTO(Date startDate, Date endDate, Long userId) throws NoStatsAvailableException {
    int presentCount = attendanceRepository.countPresentAttendancesOfUserBetweenDates(startDate, endDate, userId, AttendanceStatus.ON_TIME, AttendanceStatus.LATE);
    int absentCount = attendanceRepository.countAbsentAttendancesOfUserBetweenDates(startDate, endDate, userId, AttendanceStatus.ABSENT);
    int leaveCount = attendanceRepository.countLeaveAttendancesOfUserBetweenDates(startDate, endDate, userId, AttendanceStatus.ON_LEAVE);
    List<Long> attendanceIds = attendanceRepository.getAttendanceIdsOfUserBetweenDates(startDate, endDate, userId);
    String averageCheckIns = "-";
    String averageCheckOuts = "-";
    try {
      averageCheckIns = checkInServices.getAverageCheckInOfAttendances(attendanceIds);
      averageCheckOuts = checkOutServices.getAverageCheckOutOfAttendances(attendanceIds);
    } catch (NoStatsAvailableException exception) {
      if (absentCount > 0) {
        return new AttendanceStatsDTO(presentCount, absentCount, leaveCount, averageCheckIns, averageCheckOuts);
      } else {
        throw new NoStatsAvailableException();
      }
    }
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
