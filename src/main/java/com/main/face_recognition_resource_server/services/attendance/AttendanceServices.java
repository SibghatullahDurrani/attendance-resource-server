package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.exceptions.AttendanceDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;
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

  List<AttendanceOverviewDTO> getUserAttendanceOverview(int month, int year, Long userId) throws NoStatsAvailableException;

  AttendanceSnapshotDTO getUserAttendanceSnapshots(int year, int month, int day, Long userId) throws NoStatsAvailableException;
}
