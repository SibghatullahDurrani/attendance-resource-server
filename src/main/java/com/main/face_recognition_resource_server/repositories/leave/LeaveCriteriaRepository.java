package com.main.face_recognition_resource_server.repositories.leave;

import com.main.face_recognition_resource_server.DTOS.leave.OrganizationLeaveRecordDTO;
import com.main.face_recognition_resource_server.domains.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface LeaveCriteriaRepository {
  Page<OrganizationLeaveRecordDTO> getOrganizationLeaves(Specification<Leave> specification, Pageable pageable);
}
