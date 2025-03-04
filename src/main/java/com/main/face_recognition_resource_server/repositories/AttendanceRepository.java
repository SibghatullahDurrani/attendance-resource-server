package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.UserAttendanceDTO;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.domains.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  @Query("""
          SELECT a FROM Attendance a WHERE a.user.id = ?1 AND a.date BETWEEN ?2 AND ?3
          """)
  Optional<Attendance> getAttendanceByUserIdAndDate(Long userId, Date dateStart, Date dateEnd);

  @Query("""
          SELECT DISTINCT a.user.id FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.department.organization.id = ?3
          """)
  Set<Long> getUserIdsOfAttendance(Date startDate, Date endDate, Long organizationId);

  @Query("""
          SELECT DISTINCT a FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.department.organization.id = ?3 AND a.status = ?4 OR a.status = ?5
          """)
  List<Attendance> getPresentAttendanceOfOrganizationBetweenTime(Date startDate, Date endDate, Long organizationId, AttendanceStatus onTime, AttendanceStatus late);

  @Query("""
          SELECT DISTINCT new com.main.face_recognition_resource_server.DTOS.UserAttendanceDTO(
            a.id, a.user.id, a.date
          ) FROM Attendance a WHERE a.date BETWEEN ?2 AND ?3 AND a.user.id = ?1
          """)
  Optional<UserAttendanceDTO> getAttendanceDTOByUserIdAndDate(Long userId, Date startDate, Date endDate);

  @Query("""
          SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM Attendance a 
          WHERE a.date >= ?1 and a.user.department.organization.id = ?2
          """)
  boolean existsByDateAndOrganizationId(Date dateAfter, Long organizationId);
}