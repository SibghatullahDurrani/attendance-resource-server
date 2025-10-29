package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceAnalyticsReportRecordDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.CheckInCheckOutReportRecordDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.DailyUserAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.OrganizationUserAttendanceDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;

public interface AttendanceCriteriaRepository {
    Page<DailyUserAttendanceDTO> getDailyUserAttendances(Specification<Attendance> specification, Pageable pageable) throws IOException;

    Page<OrganizationUserAttendanceDTO> getOrganizationMonthlyUserAttendances(Specification<Attendance> specification, Pageable pageable);

    Page<CheckInCheckOutReportRecordDTO> getCheckInCheckOutReportPage(Specification<Attendance> specification, Pageable pageable);

    Page<AttendanceAnalyticsReportRecordDTO> getAttendanceAnalyticsReportPage(Specification<Attendance> specification, Pageable pageable);
}
