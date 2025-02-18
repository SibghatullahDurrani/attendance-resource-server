package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.AttendanceRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

@Service
public class AttendanceServicesImpl implements AttendanceServices {
  private final AttendanceRepository attendanceRepository;
  private final UserServices userServices;
  private final CheckInServices checkInServices;


  public AttendanceServicesImpl(AttendanceRepository attendanceRepository, UserServices userServices, CheckInServices checkInServices) {
    this.attendanceRepository = attendanceRepository;
    this.userServices = userServices;
    this.checkInServices = checkInServices;
  }

  @Override
  @Transactional
  public void markAttendance(Long userId, Date date, BufferedImage image) throws UserDoesntExistException, IOException {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    Date startDate = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime();
    Optional<Attendance> attendance = attendanceRepository.getAttendanceByUserIdAndDate(userId, startDate, date);
    if (attendance.isEmpty()) {
      User user = userServices.getUserById(userId);
      Attendance attendanceAdded = attendanceRepository.saveAndFlush(
              Attendance.builder()
                      .user(user)
                      .date(date)
                      .build()
      );
      checkInServices.saveCheckIn(date, attendanceAdded, image);
    } else {
      checkInServices.saveCheckIn(date, attendance.get(), image);
    }
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
