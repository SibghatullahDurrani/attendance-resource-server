package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.CheckInDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface CheckInServices {
  void saveCheckIn(Date date, Attendance attendance, BufferedImage image) throws IOException;

  List<CheckInDTO> getCheckInsByAttendanceId(Long attendanceId);

  String getAverageCheckInOfAttendances(List<Long> attendanceIds) throws NoStatsAvailableException;
}
