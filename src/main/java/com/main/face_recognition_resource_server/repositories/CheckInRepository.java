package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.CheckInDTO;
import com.main.face_recognition_resource_server.domains.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.CheckInDTO(
          ci.date, ci.imagePath
          )FROM CheckIn ci WHERE ci.attendance.id = ?1
          """)
  List<CheckInDTO> getCheckInsByAttendanceId(Long attendanceId);
}
