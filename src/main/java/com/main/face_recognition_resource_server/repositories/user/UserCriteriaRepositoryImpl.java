package com.main.face_recognition_resource_server.repositories.user;

import com.main.face_recognition_resource_server.DTOS.user.ShiftAllocationDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Shift;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.domains.UserShiftSetting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class UserCriteriaRepositoryImpl implements UserCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ShiftAllocationDTO> getUserShiftAllocations(Specification<User> specification, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<ShiftAllocationDTO> criteriaQuery = criteriaBuilder.createQuery(ShiftAllocationDTO.class);
        Root<User> userRoot = criteriaQuery.from(User.class);
        Join<User, Department> userDepartmentJoin = userRoot.join("department", JoinType.INNER);
        Join<User, Shift> userShiftJoin = userRoot.join("userShift", JoinType.INNER);
        Join<User, UserShiftSetting> userShiftSettingJoin = userRoot.join("userShiftSetting", JoinType.INNER);

        Predicate predicate = specification.toPredicate(userRoot, criteriaQuery, criteriaBuilder);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
        criteriaQuery.orderBy(criteriaBuilder.desc(userRoot.get("firstName")));
        criteriaQuery.select(criteriaBuilder.construct(
                ShiftAllocationDTO.class,
                userRoot.get("id"),
                userRoot.get("firstName"),
                userRoot.get("secondName"),
                userRoot.get("designation"),
                userDepartmentJoin.get("departmentName"),
                userShiftJoin.get("name"),
                userShiftSettingJoin.get("shiftMode"),
                userShiftSettingJoin.get("from"),
                userShiftSettingJoin.get("to")
        ));

        TypedQuery<ShiftAllocationDTO> typedQuery = entityManager.createQuery(criteriaQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<ShiftAllocationDTO> data = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        countQuery.select(criteriaBuilder.count(countRoot));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }
}
