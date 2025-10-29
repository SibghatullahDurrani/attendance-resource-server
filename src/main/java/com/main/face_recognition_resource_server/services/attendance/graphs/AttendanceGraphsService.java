package com.main.face_recognition_resource_server.services.attendance.graphs;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyAttendanceGraphDataDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.MonthlyAttendanceGraphDataDTO;

import java.util.List;

public interface AttendanceGraphsService {
    List<DailyAttendanceGraphDataDTO> getOrganizationAttendanceGraphsData(Long organizationId, int year, int month);

    MonthlyAttendanceGraphDataDTO getUserMonthlyAttendanceGraphData(Long userId, int year, int month);

    List<MonthlyAttendanceGraphDataDTO> getUserYearlyAttendanceGraphData(Long userId, int year);
}