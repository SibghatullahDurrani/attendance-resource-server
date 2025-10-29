package com.main.face_recognition_resource_server.services.attendance.reports;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface AttendanceReportsService {
    MonthlyAttendanceCalendarDTO getUserAttendanceCalendar(int month, int year, Long userId) throws NoStatsAvailableException;

    List<MonthlyAttendanceCalendarRecordDTO> getUserAttendanceCalendar(int year, Long userId);

    List<UserAttendanceTableRowDTO> getMonthlyUserAttendanceTable(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException, IOException;

//    List<UserAttendanceDTO> getMonthlyUserAttendanceOverview(int month, int year, Long userId) throws NoStatsAvailableException, UserDoesntExistException;

//    AttendanceSnapshotDTO getUserAttendanceSnapshots(int year, int month, int day, Long userId) throws NoStatsAvailableException;

//    Page<UserAttendanceDTO> getYearlyUserAttendanceTable(Pageable pageRequest, int year, Long userId);

    Page<DailyUserAttendanceDTO> getDailyUserAttendances(Long organizationId, AttendanceType attendanceType, AttendanceStatusFilter attendanceStatus, String userName, List<Long> departmentIds, Pageable pageable) throws IOException;

    Page<OrganizationUserAttendanceDTO> getOrganizationMonthlyUserAttendances(Pageable pageRequest, int year, int month, String fullName, Long departmentId, Long organizationId);

    Page<CheckInCheckOutReportRecordDTO> getCheckInCheckOutReportPageOfOrganization(Pageable pageRequest, Long organizationId, AttendanceReportRequestParams params);

    Page<AttendanceAnalyticsReportRecordDTO> getAttendanceAnalyticsReportPageOfOrganization(Pageable pageRequest, Long organizationId, AttendanceReportRequestParams params);
}
