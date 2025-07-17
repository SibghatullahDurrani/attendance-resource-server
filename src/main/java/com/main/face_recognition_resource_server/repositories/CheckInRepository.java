package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
  @Query("""
          SELECT ci.date FROM CheckIn ci
          WHERE ci.attendance.id IN ?1 
          """)
  List<Date> getCheckInDatesOfAttendanceIds(List<Long> attendanceIds);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO(
                    ci.faceImageName, ci.date
          ) FROM CheckIn ci
          WHERE ci.attendance.id = ?1
          """)
  List<GetAttendanceSnapPathDTO> getCheckInSnapPathsOfAttendance(Long attendanceId);

  @Query("""
          SELECT ci.date FROM CheckIn ci
          WHERE ci.attendance.id = ?1
          """)
  List<Date> getCheckInDatesOfAttendanceId(Long attendanceId);

  List<CheckIn> attendance(Attendance attendance);

  //  @Query("""
//          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO(
//          c.attendance.user.id, MIN(c.date), c.fullImageName, c.faceImageName
//          ) FROM CheckIn c WHERE c.attendance.id IN ?1 GROUP BY c.attendance.user.id,
//          c.fullImageName, c.faceImageName
//          """)
  @Query(value = """
          SELECT
          sub.user_id AS userId,
          sub.check_in_date AS date,
          sub.face_image_name AS faceImageName,
          sub.full_image_name AS fullImageName
          FROM (
            SELECT
            u.id AS user_id,
            ci.date AS check_in_date,
            ci.face_image_name AS face_image_name,
            ci.full_image_name AS full_image_name,
            ROW_NUMBER() OVER (PARTITION BY ci.attendance_id ORDER BY ci.date ASC) AS rn
            FROM check_ins ci
            INNER JOIN attendances a ON ci.attendance_id = a.id
            INNER JOIN users u ON a.user_id = u.id
            WHERE a.id IN (:attendanceIds)
          ) sub
          WHERE sub.rn = 1
          """, nativeQuery = true)
  List<RecentAttendanceDTO> getFirstCheckInsOfAttendanceIds(@Param("attendanceIds") List<Long> attendanceIds);

  //  @Query("""
//SELECT FROM CheckIn ci
//""")
  Date getFirstCheckInDateOfAttendanceId(Long id);
}
