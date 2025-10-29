package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.FlatAttendanceExcelChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.projections.attendance.CalendarAttendanceProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance>, AttendanceCriteriaRepository {

    @Query("""
            SELECT DISTINCT a FROM Attendance a
            WHERE a.user.id = :userId AND (a.date >= :dateStart AND a.date < :dateEnd)
            """)
    Optional<Attendance> getAttendanceByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("dateStart") Instant dateStart,
            @Param("dateEnd") Instant dateEnd);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0
            THEN TRUE ELSE FALSE END FROM Attendance a
            WHERE a.date >= :date
            AND a.status != "ON_LEAVE"
            AND a.user.department.organization.id = :organizationId
            """)
    boolean attendanceExistsByOrganizationIdAndDate(
            @Param("organizationId") Long organizationId,
            @Param("date") Instant date
    );

    @Query("""
            SELECT a.id FROM Attendance a
            WHERE (a.date >= :startDate AND a.date < :endDate)
            AND a.user.id = :userId
            """)
    List<Long> getAttendanceIdsOfUserBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.projections.attendance.CalendarAttendanceProjection(
                      a.date, a.status
            )FROM Attendance a
            WHERE (a.date >= :startDate AND a.date < :endDate) AND a.user.id = :userId
            """)
    List<CalendarAttendanceProjection> getCalendarAttendanceProjectionOfUserBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceDTO(
            a.id, a.date, a.status)
            FROM Attendance a WHERE (a.date >= :startDate AND a.date < :endDate) AND a.user.id = :userId
            """)
    List<UserAttendanceDTO> getAttendanceOverviewOfUserBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT a.id FROM Attendance a WHERE (a.date >= :startDate AND a.date < :endDate) AND a.user.id = :userId
            """)
    Optional<Long> getAttendanceIdOfUserBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT a.status FROM Attendance a WHERE a.id = ?1
            """)
    Optional<AttendanceStatus> getAttendanceStatusOfAttendance(Long attendanceId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceDTO(
            a.id, a.date, a.status)
            FROM Attendance a WHERE a.user.id = :userId and (a.date >= :startDate AND a.date < :endDate)
            """)
    Page<UserAttendanceDTO> getUserAttendancePageBetweenDate(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("pageRequest") Pageable pageRequest);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.UserAttendanceTableRowDTO(
            a.id, a.date, a.status
            ) FROM Attendance a WHERE a.user.id = :userId AND (a.date >= :startDate AND a.date < :endDate)
            """)
    List<UserAttendanceTableRowDTO> getAttendanceTableRecordsOfUserBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCountDTO(
                      count(a) FILTER(WHERE a.status = 'ON_TIME' OR a.status = 'LATE'),
                      count(a) FILTER(WHERE a.status = 'ABSENT'),
                      count(a) FILTER(WHERE a.status = 'ON_LEAVE'),
                      count(a) FILTER(WHERE a.status = 'LATE')
            ) FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate AND a.user.id = :userId
            """)
    AttendanceCountDTO getAttendanceCountOfUserBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT a.id FROM Attendance a
            WHERE a.user.id IN :userIds AND a.date = :date AND (a.status = "ON_TIME" OR a.status = "LATE")
            """)
    List<Long> getAllAttendanceIdsOfPresentUsersOfDate(
            @Param("userIds") List<Long> userIds,
            @Param("date") Instant date);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.DepartmentAttendanceStatsDTO(
                      a.user.department.id,
                      a.user.department.departmentName,
                      COUNT(*) FILTER(WHERE a.status = "ON_TIME" OR a.status = "LATE"),
                      COUNT(*) FILTER(WHERE a.status = "LATE"),
                      COUNT(*) FILTER(WHERE a.status = "ABSENT"),
                      COUNT(*) FILTER(WHERE a.status =  "ON_LEAVE"),
                      COUNT(*) FILTER(WHERE a.status = "ON_TIME"),
                      COUNT(*)
            ) FROM Attendance a
            WHERE a.user.department.id = :departmentId AND (a.date >= :startDate AND a.date <= :endDate)
            GROUP BY a.user.department.id, a.user.department.departmentName
            """)
    Optional<DepartmentAttendanceStatsDTO> getDepartmentAttendance(
            @Param("departmentId") Long departmentId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("""
            SELECT new  com.main.face_recognition_resource_server.DTOS.attendance.DailyAttendanceGraphDataDTO(
                      a.date,
                      COUNT(*) FILTER(WHERE a.status = 'ON_TIME' OR a.status = 'LATE'),
                      COUNT(*) FILTER(WHERE a.status = 'LATE'),
                      COUNT(*) FILTER(WHERE a.status = 'ABSENT'),
                      COUNT(*) FILTER(WHERE a.status = 'ON_LEAVE')
            ) FROM Attendance a WHERE a.user.department.organization.id = :organizationId AND (a.date >= :startDate AND a.date <= :endDate)
            GROUP BY a.date
            """)
    List<DailyAttendanceGraphDataDTO> getOrganizationAttendanceChartInfo(
            @Param("organizationId") Long organizationId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);


    @Query("""
            SELECT new  com.main.face_recognition_resource_server.DTOS.attendance.MonthlyAttendanceGraphDataDTO(
                      MONTH(a.date),
                      COUNT(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'LATE' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'ON_LEAVE' THEN 1 END)
            ) FROM Attendance a WHERE a.user.id = :userId AND (a.date BETWEEN :startDate AND :endDate)
            GROUP BY MONTH(a.date)
            """)
    Optional<MonthlyAttendanceGraphDataDTO> getUserAttendanceGraphData(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

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
                AND a.user_id = :userId
                AND extract(YEAR FROM a.date) = :year
              GROUP BY m.month
              ORDER BY m.month
            """, nativeQuery = true)
    List<MonthlyAttendanceGraphDataDTO> getUserYearlyAttendanceGraphData(
            @Param("userId") Long userId,
            @Param("year") int year);

    @Query("""
            SELECT a.user.id FROM Attendance a WHERE a.date = :date AND a.status = 'ON_LEAVE'
            """)
    List<Long> getUserIdsOfLeaveOfDate(@Param("date") Instant date);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.OrganizationAttendanceStatsDTO(
                      COUNT(a),
                      COUNT(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'ON_TIME' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'ON_LEAVE' THEN 1 END),
                      COUNT(CASE WHEN a.status = 'LATE' THEN 1 END)
            )FROM Attendance a WHERE a.user.department.organization.id = :organizationId AND a.date = :today
            """)
    OrganizationAttendanceStatsDTO getOrganizationAttendanceStatisticsForDate(
            @Param("organizationId") Long organizationId,
            @Param("today") Instant today);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO(
                        u.id,u.firstName, u.secondName, u.department.departmentName,u.designation
            ) FROM User u WHERE u.id IN :userIds
            AND EXISTS (
                SELECT 1 FROM Attendance a
                WHERE a.user.id = u.id
                AND (a.date >= :startDate AND a.date <= :endDate)
            )
            """)
    List<AttendanceExcelDataDTO> getUsersAttendanceExcelData(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userIds") List<Long> userIds);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO(
                        u.id,u.firstName, u.secondName, u.department.departmentName,u.designation
            ) FROM User u WHERE u.department.id IN :departmentIds
            AND EXISTS (
                SELECT 1 FROM Attendance a
                WHERE a.user.id = u.id
                AND (a.date BETWEEN :startDate AND :endDate)
            )
            """)
    List<AttendanceExcelDataDTO> getDepartmentsAttendanceExcelData(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("departmentIds") List<Long> departmentIds);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO(
                        a.date, a.status, a.id
            ) FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate AND a.user.id = :userId
            """)
    List<ExcelAttendanceDTO> getUserExcelAttendance(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") Long userId);

    @Query("""
            SELECT new  com.main.face_recognition_resource_server.DTOS.export.FlatAttendanceExcelChartDTO(
                a.user.department.id,
                a.user.department.departmentName,
                a.date,
                SUM(CASE WHEN a.status = 'ON_TIME' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ON_LEAVE' THEN 1 ELSE 0 END)
            ) FROM Attendance a
            WHERE a.user.department.id IN :departmentIds
                AND (a.date BETWEEN :fromDate AND :toDate)
            GROUP BY a.date, a.user.department.id, a.user.department.departmentName
            ORDER BY a.user.department.id, a.date
            """)
    List<FlatAttendanceExcelChartDTO> getDepartmentLineChartData(
            @Param("departmentIds") List<Long> departmentIds,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.export.UserAttendancePieChartDTO(
                a.user.id,
                a.user.firstName,
                a.user.secondName,
                SUM(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ON_LEAVE' THEN 1 ELSE 0 END)
            )
            FROM Attendance a
                WHERE (a.date BETWEEN :startDate AND :endDate)
                AND a.user.id IN :userIds
            GROUP BY a.user.id, a.user.firstName, a.user.secondName
            """)
    List<UserAttendancePieChartDTO> getUsersAttendancePieChartData(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userIds") List<Long> userIds);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO(
                        u.id,u.firstName, u.secondName, u.department.departmentName,u.designation
            ) FROM User u WHERE u.department.organization.id = :organizationId
            AND EXISTS (
                SELECT 1 FROM Attendance a
                WHERE a.user.id = u.id
                AND (a.date >= :startDate AND a.date <= :endDate)
            )
            """)
    List<AttendanceExcelDataDTO> getOrganizationAttendanceData(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("organizationId") Long organizationId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO(
                a.user.department.id,
                a.user.department.departmentName,
                SUM(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ON_LEAVE' THEN 1 ELSE 0 END)
            )
            FROM Attendance a
                WHERE a.date BETWEEN :startDate AND :endDate
                AND a.user.department.organization.id = :organizationId
            GROUP BY a.user.department.id, a.user.department.departmentName
            """)
    List<DepartmentAttendancePieChartDTO> getOrganizationPieChartData(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("organizationId") Long organizationId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendancePieChartDTO(
                a.user.department.id,
                a.user.department.departmentName,
                SUM(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ON_LEAVE' THEN 1 ELSE 0 END)
            )
            FROM Attendance a
                WHERE a.date BETWEEN :startDate AND :endDate
                AND a.user.department.id IN :departmentIds
            GROUP BY a.user.department.id, a.user.department.departmentName
            """)
    List<DepartmentAttendancePieChartDTO> getDepartmentsAttendancePieChartData(
            @Param("departmentIds") List<Long> departmentIds,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("""
            SELECT new  com.main.face_recognition_resource_server.DTOS.export.FlatAttendanceExcelChartDTO(
                a.user.department.id,
                a.user.department.departmentName,
                a.date,
                SUM(CASE WHEN a.status = 'ON_TIME' OR a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ON_LEAVE' THEN 1 ELSE 0 END)
            ) FROM Attendance a
            WHERE a.user.department.organization.id = :organizationId
                AND (a.date BETWEEN :fromDate AND :toDate)
            GROUP BY a.date, a.user.department.id, a.user.department.departmentName
            ORDER BY a.user.department.id, a.date
            """)
    List<FlatAttendanceExcelChartDTO> getOrganizationLineChartData(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}