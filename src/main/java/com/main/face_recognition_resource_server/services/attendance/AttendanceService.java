package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface AttendanceService {
    void markCheckIn(Long userId, Date endDate, BufferedImage fullImage, BufferedImage faceImage) throws UserDoesntExistException, IOException;

    Set<Long> getCache(Long organizationId, CameraType type);

    void markCheckOut(Long userId, Date endDate, BufferedImage fullImage, BufferedImage faceImage) throws IOException, UserDoesntExistException;

    void markAbsentOfAllUsersInOrganizationForCurrentDay(Long OrganizationId);

    void markAbsentOfAllUsersForCurrentDay();

    AttendanceStatsDTO getUserAttendanceStats(int year, Long userId) throws NoStatsAvailableException;

    AttendanceStatsDTO getUserAttendanceStats(int month, int year, Long userId) throws NoStatsAvailableException;

    AttendanceCalendarDTO getUserAttendanceCalendar(int month, int year, Long userId) throws NoStatsAvailableException;

    List<UserAttendanceTableDTO> getMonthlyUserAttendanceTable(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException, IOException;

    List<UserAttendanceDTO> getMonthlyUserAttendanceOverview(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException;

    AttendanceSnapshotDTO getUserAttendanceSnapshots(int year, int month, int day, Long userId) throws NoStatsAvailableException;

    Page<UserAttendanceDTO> getYearlyUserAttendanceTable(Pageable pageRequest, int year, Long userId);

    List<MonthlyAttendanceCalendarRecordDTO> getYearlyUserAttendanceCalendar(int year, String userName);

    void sendLiveAttendanceFeed(Long organizationId, AttendanceLiveFeedDTO attendanceLiveFeedDTO);

    List<AttendanceLiveFeedDTO> getRecentAttendancesOfOrganization(long organizationId);

    List<DepartmentAttendanceDTO> getOrganizationDepartmentsAttendance(Long organizationId, int year, int month, int day) throws DepartmentDoesntExistException;

    Page<DailyUserAttendanceDTO> getDailyUserAttendances(Long organizationId, AttendanceType attendanceType, AttendanceStatusFilter attendanceStatus, String userName, List<Long> departmentIds, Pageable pageable) throws IOException;

    List<DailyAttendanceGraphDataDTO> getOrganizationAttendanceGraphsData(Long organizationId, int year, int month);

    MonthlyAttendanceGraphDataDTO getUserMonthlyAttendanceGraphData(Long userId, int year, int month);

    List<MonthlyAttendanceGraphDataDTO> getUserYearlyAttendanceGraphData(Long userId, int year);

    void markLeaveOfUserOnDate(Long userId, Date date);

    OrganizationAttendanceStatisticsDTO getCurrentDayOrganizationAttendanceStatistics(Long organizationId);

    Page<OrganizationUserAttendanceDTO> getOrganizationMonthlyUserAttendances(Pageable pageRequest, int year, int month, String fullName, Long departmentId, Long organizationId);

    void markAbsentOfUserOnToday(User user);

}
