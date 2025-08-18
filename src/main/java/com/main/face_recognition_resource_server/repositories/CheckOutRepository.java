package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {

    @Query("""
            SELECT co.date FROM CheckOut co
            WHERE co.attendance.id IN ?1
            """)
    List<Date> getCheckOutDatesOfAttendanceIds(List<Long> attendanceIds);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO(
                      co.faceImageName, co.date
            ) FROM CheckOut co
            WHERE co.attendance.id = ?1
            """)
    List<GetAttendanceSnapPathDTO> getCheckOutSnapPathsOfAttendance(Long attendanceId);

    @Query("""
            SELECT co.date FROM CheckOut co
            WHERE co.attendance.id = ?1
            """)
    List<Date> getCheckOutDatesOfAttendanceId(Long attendanceId);

    List<CheckOut> attendance(Attendance attendance);

    @Query("""
              SELECT new com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO(
              c.attendance.user.id, c.date, c.fullImageName, c.faceImageName
              ) FROM CheckOut c WHERE c.attendance.id IN ?1 ORDER BY c.date DESC LIMIT 5
            """)
    List<RecentAttendanceDTO> getRecentCheckOutsOfAttendanceIds(List<Long> attendanceIds);

    @Query("""
            SELECT MAX(c.date) FROM CheckOut c WHERE c.attendance.id = ?1
            
            """)
    Date getLastCheckOutDateOfAttendanceId(Long attendanceId);
}
