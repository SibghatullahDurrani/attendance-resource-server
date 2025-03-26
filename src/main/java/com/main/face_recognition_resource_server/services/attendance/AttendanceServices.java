package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface AttendanceServices {
  void markCheckIn(Long userId, Date endDate, BufferedImage fullImage, BufferedImage faceImage) throws UserDoesntExistException, IOException;

  Set<Long> getCache(Long organizationId, CameraType type);

  void markCheckOut(Long userId, Date endDate, BufferedImage fullImage, BufferedImage faceImage) throws IOException;

  void markAbsentOfAllUsersInOrganizationForCurrentDay(Long OrganizationId);

  AttendanceStatsDTO getUserAttendanceStats(int year, Long userId) throws NoStatsAvailableException;

  AttendanceStatsDTO getUserAttendanceStats(int month, int year, Long userId) throws NoStatsAvailableException;

  AttendanceCalendarDTO getUserAttendanceCalendar(int month, int year, Long userId) throws NoStatsAvailableException;

  List<UserAttendanceTableDTO> getMonthlyUserAttendanceTable(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException, IOException;

  List<UserAttendanceDTO> getMonthlyUserAttendanceOverview(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException;

  AttendanceSnapshotDTO getUserAttendanceSnapshots(int year, int month, int day, Long userId) throws NoStatsAvailableException;

  Page<UserAttendanceDTO> getYearlyUserAttendanceTable(Pageable pageRequest, int year, Long userId);
}
