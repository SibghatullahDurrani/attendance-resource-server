package com.main.face_recognition_resource_server.repositories.user;

import com.main.face_recognition_resource_server.DTOS.user.ShiftAllocationDTO;
import com.main.face_recognition_resource_server.DTOS.user.UsersOfOwnOrganizationRecordDTO;
import com.main.face_recognition_resource_server.domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserCriteriaRepository {
    Page<ShiftAllocationDTO> getUserShiftAllocations(Specification<User> specification, Pageable pageable, boolean isFilterApplied);

    Page<UsersOfOwnOrganizationRecordDTO> getUsersPageOfOrganization(Specification<User> specification, Pageable pageable);
}
