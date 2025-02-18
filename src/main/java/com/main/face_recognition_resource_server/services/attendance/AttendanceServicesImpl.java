package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.repositories.AttendanceRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttendanceServicesImpl implements AttendanceServices {
  private final AttendanceRepository attendanceRepository;
  private final UserServices userServices;


  public AttendanceServicesImpl(AttendanceRepository attendanceRepository, UserServices userServices) {
    this.attendanceRepository = attendanceRepository;
    this.userServices = userServices;
  }

  @Override
  public void markAttendance(Long userId, Date date) throws UserDoesntExistException {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    Date startDate = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime();
    Optional<Attendance> attendance = attendanceRepository.getAttendanceByUserIdAndDate(userId, startDate, date);
    if (attendance.isEmpty()) {
      User user = userServices.getUserById(userId);
      List<CheckIn> checkIns = List.of(
              CheckIn.builder()
                      .date(date)
                      .build()
      );
      attendanceRepository.saveAndFlush(
              Attendance.builder()
                      .user(user)
                      .date(date)
                      .checkIns(checkIns)
                      .build()
      );
    } else {
      Attendance editedAttendance = attendance.get();
      List<CheckIn> checkIns = editedAttendance.getCheckIns();
      checkIns.add(CheckIn.builder()
              .date(date)
              .build());
      editedAttendance.setCheckIns(checkIns);
      attendanceRepository.saveAndFlush(editedAttendance);
    }
  }
}
