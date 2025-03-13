package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.attendance.CheckInDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.domains.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.CheckInDTO(
          ci.date, ci.imagePath
          )FROM CheckIn ci WHERE ci.attendance.id = ?1
          """)
  List<CheckInDTO> getCheckInsOfAttendanceId(Long attendanceId);

  @Query("""
          SELECT ci.date FROM CheckIn ci
          WHERE ci.attendance.id IN ?1
          """)
  List<Date> getCheckInDatesOfAttendanceIds(List<Long> attendanceIds);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO(
                    ci.imagePath, ci.date
          ) FROM CheckIn ci
          WHERE ci.attendance.id = ?1
          """)
  List<GetAttendanceSnapPathDTO> getCheckInSnapPathsOfAttendance(Long attendanceId);

  @Query("""
          SELECT ci.date FROM CheckIn ci
          WHERE ci.attendance.id = ?1
          """)
  List<Date> getCheckInDatesOfAttendanceId(Long attendanceId);
}
