package com.main.face_recognition_resource_server.repositories.shift;

import com.main.face_recognition_resource_server.domains.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ShiftRepository extends JpaRepository<Shift, Long>, JpaSpecificationExecutor<Shift>, ShiftCriteriaRepository {
    @Query("""
            SELECT COUNT(s.organization) FROM Shift s WHERE s.organization.id = ?1
            """)
    Long getOrganizationShiftCount(Long organizationId);
}
