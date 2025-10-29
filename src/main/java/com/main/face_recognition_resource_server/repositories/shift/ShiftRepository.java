package com.main.face_recognition_resource_server.repositories.shift;

import com.main.face_recognition_resource_server.DTOS.shift.ShiftOptionDTO;
import com.main.face_recognition_resource_server.constants.shift.WorkingDays;
import com.main.face_recognition_resource_server.domains.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ShiftRepository extends JpaRepository<Shift, Long>, JpaSpecificationExecutor<Shift>, ShiftCriteriaRepository {
    @Query("""
            SELECT COUNT(s.organization) FROM Shift s WHERE s.organization.id = ?1
            """)
    Long getOrganizationShiftCount(Long organizationId);

    @Query("""
            SELECT new com.main.face_recognition_resource_server.DTOS.shift.ShiftOptionDTO(
                        s.id, s.name, s.isDefault
            )FROM Shift s WHERE s.organization.id = ?1 ORDER BY s.name ASC
            """)
    List<ShiftOptionDTO> getShiftOptionsByOrganizationId(Long organizationId);

    @Query("""
            SELECT u.userShift.workingDays FROM User u WHERE u.id = :userId
            """)
    Set<WorkingDays> getWorkingDaysOfUser(@Param("userId") Long userId);
}
