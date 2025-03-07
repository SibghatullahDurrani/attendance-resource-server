package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.AttendanceCalendarDTO;
import com.main.face_recognition_resource_server.DTOS.AttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.UserAttendanceDTO;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.exceptions.AttendanceDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public interface AttendanceServices {
  void markCheckIn(Long userId, Date endDate, BufferedImage image) throws UserDoesntExistException, IOException;

  Set<Long> getCache(Long organizationId, CameraType type);

  void markCheckOut(Long userId, Date endDate, BufferedImage image) throws IOException;

  UserAttendanceDTO getAttendanceOfUserOnDate(Long userId, String date) throws UserDoesntExistException, AttendanceDoesntExistException;

  void markAbsentOfAllUsersInOrganizationForCurrentDay(Long OrganizationId);

  AttendanceStatsDTO getUserAttendanceStats(int year, Long userId) throws NoStatsAvailableException;

  AttendanceStatsDTO getUserAttendanceStats(int month, int year, Long userId) throws NoStatsAvailableException;

  AttendanceCalendarDTO getUserAttendanceCalendar(int month, int year, Long userId) throws NoStatsAvailableException;
}
