package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.domains.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO(
                    l.id, l.date, l.status
          ) FROM Leave l WHERE l.date BETWEEN ?1 AND ?2 AND l.user.username = ?3
          """)
  List<LeaveDTO> getUserLeavesBetweenDates(Date startDate, Date endDate, String username);
}
