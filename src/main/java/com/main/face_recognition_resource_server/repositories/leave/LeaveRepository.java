package com.main.face_recognition_resource_server.repositories.leave;

import com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeaveDataWithApplicationDTO;
import com.main.face_recognition_resource_server.constants.leave.LeaveStatus;
import com.main.face_recognition_resource_server.domains.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LeaveRepository extends JpaRepository<Leave, Long>, JpaSpecificationExecutor<Leave>, LeaveCriteriaRepository {
    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.leave.LeaveDTO(
                      l.id, l.date, l.status
            ) FROM Leave l WHERE l.date BETWEEN ?1 AND ?2 AND l.user.username = ?3
            """)
    List<LeaveDTO> getUserLeavesBetweenDates(Date startDate, Date endDate, String username);

    @Query("""
            SELECT l.user.department.organization.id FROM Leave l WHERE l.id = ?1
            """)
    Long getOrganizationIdOfLeave(Long leaveId);

    @Query("""
            SELECT l.leaveApplication FROM Leave l WHERE l.id = ?1
            """)
    Optional<String> getLeaveApplication(Long leaveId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.leave.LeaveDataWithApplicationDTO(
            l.id, l.date, l.status, l.type, l.user.firstName, l.user.secondName, l.user.department.departmentName, l.leaveApplication
            ) FROM Leave l WHERE l.id = ?1
            """)
    Optional<LeaveDataWithApplicationDTO> getLeaveDataWithApplication(Long leaveId);

    @Query("""
            SELECT l.user.id FROM Leave l WHERE l.id = ?1
            """)
    Optional<Long> getUserIdOfLeave(Long leaveId);

    @Query("""
            UPDATE Leave l SET l.status = ?1 WHERE l.id = ?2
            """)
    @Modifying
    @Transactional
    void changeLeaveStatus(LeaveStatus leaveStatus, Long leaveId);
}
