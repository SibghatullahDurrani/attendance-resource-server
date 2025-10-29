package com.main.face_recognition_resource_server.repositories.leave;

import com.main.face_recognition_resource_server.DTOS.leave.OrganizationUserLeaveRecordDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Leave;
import com.main.face_recognition_resource_server.domains.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class LeaveCriteriaRepositoryImpl implements LeaveCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<OrganizationUserLeaveRecordDTO> getOrganizationLeaves(Specification<Leave> specification, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<OrganizationUserLeaveRecordDTO> criteriaQuery = criteriaBuilder.createQuery(OrganizationUserLeaveRecordDTO.class);
        Root<Leave> root = criteriaQuery.from(Leave.class);

        Join<Leave, User> leaveUserJoin = root.join("user", JoinType.INNER);
        Join<User, Department> leaveUserDepartmentJoin = leaveUserJoin.join("department", JoinType.INNER);

        Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }

        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("date")));

        criteriaQuery.select(criteriaBuilder.construct(
                OrganizationUserLeaveRecordDTO.class,
                root.get("id"),
                root.get("date"),
                root.get("status"),
                root.get("type"),
                leaveUserJoin.get("firstName"),
                leaveUserJoin.get("secondName"),
                leaveUserDepartmentJoin.get("departmentName")
        ));

        TypedQuery<OrganizationUserLeaveRecordDTO> query = entityManager.createQuery(criteriaQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<OrganizationUserLeaveRecordDTO> data = query.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Leave> countRoot = countQuery.from(Leave.class);

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        if (countPredicate != null) {
            criteriaQuery.where(countPredicate);
        }

        countQuery.select(criteriaBuilder.count(countRoot));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }
}
