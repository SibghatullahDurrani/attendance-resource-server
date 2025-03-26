package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.CheckInDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface CheckInServices {
  void saveCheckIn(Date date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException;

  List<CheckInDTO> getCheckInsByAttendanceId(Long attendanceId);

  List<Long> getCheckInTimesByAttendanceId(Long attendanceId);

  String getAverageCheckInOfAttendances(List<Long> attendanceIds) throws NoStatsAvailableException;

  List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckInSnapshotsOfAttendance(Long attendanceId);
}
