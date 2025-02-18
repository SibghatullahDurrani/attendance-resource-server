package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  @Query("""
          SELECT a FROM Attendance a WHERE a.user.id = ?1 AND a.date BETWEEN ?2 AND ?3
          """)
  Optional<Attendance> getAttendanceByUserIdAndDate(Long userId, Date dateStart, Date dateEnd);
}
