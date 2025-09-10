package com.main.face_recognition_resource_server.services.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;

import java.util.List;

public interface ExportService {
    List<AttendanceExcelDataDTO> getDepartmentAttendanceExcelData(List<Long> userIds, Long fromDate, Long toDate);

    List<AttendanceExcelDataDTO> getDepartmentAttendanceExcelData(List<Long> userIds, int month, int year);

    List<AttendanceExcelDataDTO> getDepartmentAttendanceExcelData(List<Long> departmentIds, Long singleDate);

    List<AttendanceExcelDataDTO> getUserAttendanceExcelData(List<Long> userIds, Long fromDate, Long toDate);

    List<AttendanceExcelDataDTO> getUserAttendanceExcelData(List<Long> userIds, int month, int year);

    List<AttendanceExcelDataDTO> getUserAttendanceExcelData(List<Long> userIds, Long singleDate);

    List<DepartmentAttendanceLineChartDTO> getDepartmentsAttendanceLineChartData(List<Long> departmentIds, Long toDate, Long fromDate);

    List<DepartmentAttendanceLineChartDTO> getDepartmentsAttendanceLineChartData(List<Long> departmentIds, int month, int year);

    List<UserAttendancePieChartDTO> getUserAttendancePieChartData(List<Long> userIds, Long fromDate, Long toDate);

    List<UserAttendancePieChartDTO> getUserAttendancePieChartData(List<Long> userIds, int month, int year);

    List<UserAttendancePieChartDTO> getUserAttendancePieChartData(List<Long> userIds, Long singleDate);

    List<AttendanceExcelDataDTO> getOrganizationExcelData(Long organizationId, Long fromDate, Long toDate);

    List<AttendanceExcelDataDTO> getOrganizationExcelData(Long organizationId, int month, int year);

    List<AttendanceExcelDataDTO> getOrganizationExcelData(Long organizationId, Long singleDate);

    List<DepartmentAttendanceLineChartDTO> getOrganizationLineChartData(Long organizationId, Long fromDate, Long toDate);

    List<DepartmentAttendanceLineChartDTO> getOrganizationLineChartData(Long organizationId, int month, int year);

    List<DepartmentAttendancePieChartDTO> getOrganizationPieChartData(Long organizationId, Long singleDate);

    List<DepartmentAttendancePieChartDTO> getDepartmentAttendancePieChartData(List<Long> departmentIds, Long singleDate);
}
