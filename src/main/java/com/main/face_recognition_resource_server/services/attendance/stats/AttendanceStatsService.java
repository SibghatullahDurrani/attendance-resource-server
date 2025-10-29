package com.main.face_recognition_resource_server.services.attendance.stats;

import com.main.face_recognition_resource_server.DTOS.attendance.DepartmentAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.OrganizationAttendanceStatsDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceStatsDTO;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;

import java.util.List;

public interface AttendanceStatsService {
    UserAttendanceStatsDTO getUserAttendanceStats(int year, Long userId) throws NoStatsAvailableException;

    UserAttendanceStatsDTO getUserAttendanceStats(int month, int year, Long userId) throws NoStatsAvailableException;

    OrganizationAttendanceStatsDTO getCurrentDayOrganizationAttendanceStatistics(Long organizationId);

    List<DepartmentAttendanceStatsDTO> getOrganizationDepartmentsAttendanceStats(Long organizationId, int year, int month, int day) throws DepartmentDoesntExistException;
}
