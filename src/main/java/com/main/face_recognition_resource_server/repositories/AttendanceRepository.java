package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.attendance.CalendarAttendanceDataDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceTableDTO;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  @Query("""
          SELECT a FROM Attendance a
          WHERE a.user.id = ?1 AND a.date BETWEEN ?2 AND ?3
          """)
  Optional<Attendance> getAttendanceByUserIdAndDate(Long userId, Date dateStart, Date dateEnd);

  @Query("""
          SELECT DISTINCT a FROM Attendance a
          WHERE a.date BETWEEN ?1 AND ?2 AND
          a.user.department.organization.id = ?3 AND
          (a.status = ?4 OR a.status = ?5)
          """)
  List<Attendance> getPresentAttendanceOfOrganizationBetweenTime(Date startDate, Date endDate, Long organizationId, AttendanceStatus onTime, AttendanceStatus late);

  @Query("""
          SELECT CASE WHEN COUNT(a) > 0
          THEN TRUE ELSE FALSE END FROM Attendance a
          WHERE a.date >= ?1 and a.user.department.organization.id = ?2
          """)
  boolean existsByDateAndOrganizationId(Date dateAfter, Long organizationId);

  @Query("""
          SELECT count(a) FROM Attendance a
          WHERE a.date between ?1 and ?2 and a.user.id = ?3
          and (a.status = ?4 or a.status = ?5)
          """)
  int countPresentAttendancesOfUserBetweenDates(Date startDate, Date endDate, Long userId, AttendanceStatus onTime, AttendanceStatus late);

  @Query("""
          SELECT count(a) FROM Attendance a
          WHERE a.date between ?1 and ?2 and a.user.id = ?3
          and a.status = ?4
          """)
  int countAbsentAttendancesOfUserBetweenDates(Date startDate, Date endDate, Long userId, AttendanceStatus absent);

  @Query("""
          SELECT count(a) FROM Attendance a
          WHERE a.date between ?1 and ?2 and a.user.id = ?3
          and a.status = ?4
          """)
  int countLeaveAttendancesOfUserBetweenDates(Date startDate, Date endDate, Long userId, AttendanceStatus leave);

  @Query("""
          SELECT a.id FROM Attendance a
          WHERE a.date between ?1 and ?2
          and a.user.id = ?3
          """)
  List<Long> getAttendanceIdsOfUserBetweenDates(Date startDate, Date endDate, Long userId);

  List<Attendance> user(User user);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.CalendarAttendanceDataDTO(
          a.date, a.status
          )FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.id = ?3
          """)
  List<CalendarAttendanceDataDTO> getAttendanceStatusWithDateOfUserBetweenDates(Date startDate, Date endDate, Long userId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceDTO(
                    a.id,a.date,a.status
            )FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.id = ?3
          """)
  List<UserAttendanceDTO> getAttendanceOverviewOfUserBetweenDates(Date startDate, Date endDate, Long userId);

  @Query("""
          SELECT a.id FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.id = ?3
          """)
  Optional<Long> getAttendanceIdOfUserBetweenDates(Date startDate, Date endDate, Long userId);

  @Query("""
          SELECT a.status FROM Attendance a WHERE a.id = ?1
          """)
  AttendanceStatus getAttendanceStatusOfAttendance(Long attendanceId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceDTO(
                    a.id, a.date, a.status
          ) FROM Attendance a WHERE a.user.id = ?1 and a.date BETWEEN ?2 AND ?3
          """)
  Page<UserAttendanceDTO> getUserAttendancePageBetweenDate(Long userId, Date startDate, Date endDate, Pageable pageRequest);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceTableDTO(
          a.id, a.date, a.status
          ) FROM Attendance a WHERE a.user.id = ?3 AND a.date BETWEEN ?1 AND ?2
          """)
  List<UserAttendanceTableDTO> getAttendanceTableRecordsOfUserBetweenDates(Date startDate, Date endDate, Long userId);
}