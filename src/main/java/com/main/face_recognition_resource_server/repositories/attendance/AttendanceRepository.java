package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance>, AttendanceCriteriaRepository {

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

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCountDTO(
                    count(a) FILTER(WHERE a.status = 'ON_TIME' OR a.status = 'LATE'),
                    count(a) FILTER(WHERE a.status = 'ABSENT'),
                    count(a) FILTER(WHERE a.status = 'ON_LEAVE'),
                    count(a) FILTER(WHERE a.status = 'LATE')
          ) FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.id = ?3
          """)
  AttendanceCountDTO getAttendanceCountOfUserBetweenDates(Date startDate, Date endDate, Long userId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCountDTO(
                    count(a) FILTER(WHERE a.status = 'ON_TIME' OR a.status = 'LATE'),
                    count(a) FILTER(WHERE a.status = 'ABSENT'),
                    count(a) FILTER(WHERE a.status = 'ON_LEAVE'),
                    count(a) FILTER(WHERE a.status = 'LATE')
          ) FROM Attendance a WHERE a.date BETWEEN ?1 AND ?2 AND a.user.username = ?3
          """)
  AttendanceCountDTO getAttendanceCountOfUserBetweenDates(Date startDate, Date endDate, String userName);

  @Query("""
          SELECT a.id FROM Attendance a
          WHERE a.user.id IN ?1 AND a.date = ?2 AND (a.status = "ON_TIME" OR a.status = "LATE")
          """)
  List<Long> getAllAttendanceIdsOfTodaysPresentUsers(List<Long> userIds, Date date);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.DepartmentAttendanceDTO(
                    COUNT(*) FILTER(WHERE a.status = "ON_TIME" OR a.status = "LATE"),
                    COUNT(*) FILTER(WHERE a.status = "LATE"),
                    COUNT(*) FILTER(WHERE a.status = "ABSENT"),
                    COUNT(*) FILTER(WHERE a.status =  "ON_LEAVE")
          ) FROM Attendance a WHERE a.user.department.id = ?1 AND a.date BETWEEN ?2 AND ?3
          """)
  DepartmentAttendanceDTO getDepartmentAttendance(Long departmentIds, Date startDate, Date endDate);

  @Query("""
          SELECT new  com.main.face_recognition_resource_server.DTOS.attendance.DailyAttendanceGraphDataDTO(
                    a.date,
                    COUNT(*) FILTER(WHERE a.status = 'ON_TIME' OR a.status = 'LATE'),
                    COUNT(*) FILTER(WHERE a.status = 'LATE'),
                    COUNT(*) FILTER(WHERE a.status = 'ABSENT'),
                    COUNT(*) FILTER(WHERE a.status = 'ON_LEAVE')
          ) FROM Attendance a WHERE a.user.department.organization.id = ?1 AND a.date BETWEEN ?2 AND ?3
          GROUP BY a.date
          """)
  List<DailyAttendanceGraphDataDTO> getOrganizationAttendanceChartInfo(Long organizationId, Date startDate, Date endDate);


  @Query("""
          SELECT new  com.main.face_recognition_resource_server.DTOS.attendance.MonthlyAttendanceGraphDataDTO(
                    MONTH(a.date),
                    COUNT(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 END),
                    COUNT(CASE WHEN a.status = 'LATE' THEN 1 END),
                    COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END),
                    COUNT(CASE WHEN a.status = 'ON_LEAVE' THEN 1 END)
          ) FROM Attendance a WHERE a.user.id = ?1 AND YEAR(a.date) = ?2 AND MONTH(a.date) = ?3
          GROUP BY MONTH(a.date)
          """)
  Optional<MonthlyAttendanceGraphDataDTO> getUserAttendanceGraphData(Long userId, int year, int month);

  @Query(value = """
            SELECT
                m.month  AS "month",
                COUNT(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 END),
                COUNT(CASE WHEN a.status = 'LATE' THEN 1 END),
                COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END),
                COUNT(CASE WHEN a.status = 'ON_LEAVE' THEN 1 END)
            FROM generate_series(1,12) AS m(month)
            LEFT JOIN attendances a
              ON m.month = extract(MONTH FROM a.date)
              AND a.user_id = ?1
              AND extract(YEAR FROM a.date) = ?2
            GROUP BY m.month
            ORDER BY m.month
          """, nativeQuery = true)
  List<MonthlyAttendanceGraphDataDTO> getUserYearlyAttendanceGraphData(Long userId, int year);
}