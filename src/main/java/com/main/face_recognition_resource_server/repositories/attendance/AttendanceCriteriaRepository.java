package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyUserAttendanceDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface AttendanceCriteriaRepository {
  Page<DailyUserAttendanceDTO> getDailyUserAttendances(Specification<Attendance> specification, Pageable pageable);
}
