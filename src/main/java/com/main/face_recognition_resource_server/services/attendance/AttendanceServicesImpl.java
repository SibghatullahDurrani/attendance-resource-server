package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.AttendanceRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

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
}
