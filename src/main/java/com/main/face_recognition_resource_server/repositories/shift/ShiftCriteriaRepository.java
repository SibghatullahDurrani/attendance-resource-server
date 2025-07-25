package com.main.face_recognition_resource_server.repositories.shift;

import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import com.main.face_recognition_resource_server.domains.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ShiftCriteriaRepository {
    Page<ShiftTableRowDTO> getShifts(Specification<Shift> specification, Pageable pageable);
}
