package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.CheckOutDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface CheckOutServices {
  void saveCheckOut(Date date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException;

  List<CheckOutDTO> getCheckOutsByAttendanceId(Long attendanceId);

  String getAverageCheckOutOfAttendances(List<Long> attendanceIds) throws NoStatsAvailableException;

  List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckOutSnapshotsOfAttendance(Long attendanceId);

  List<Long> getCheckOutTimesByAttendanceId(Long attendanceId);
}
