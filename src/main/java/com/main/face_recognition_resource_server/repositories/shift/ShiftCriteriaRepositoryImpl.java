package com.main.face_recognition_resource_server.repositories.shift;

import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import com.main.face_recognition_resource_server.domains.Shift;
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

public class ShiftCriteriaRepositoryImpl implements ShiftCriteriaRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ShiftTableRowDTO> getShifts(Specification<Shift> specification, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<ShiftTableRowDTO> criteriaQuery = criteriaBuilder.createQuery(ShiftTableRowDTO.class);
        Root<Shift> root = criteriaQuery.from(Shift.class);

        Subquery<Long> userCountSubquery = criteriaQuery.subquery(Long.class);
        Root<User> userRoot = userCountSubquery.from(User.class);
        userCountSubquery.select(criteriaBuilder.count(userRoot));
        userCountSubquery.where(criteriaBuilder.equal(userRoot.get("userShift").get("id"), root.get("id")));

        Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        if (predicate != null) {
            criteriaQuery.where(predicate);
        }

        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("name")));

        criteriaQuery.select(criteriaBuilder.construct(
                ShiftTableRowDTO.class,
                root.get("id"),
                root.get("name"),
                root.get("checkInTime"),
                root.get("checkOutTime"),
                root.get("isDefault"),
                userCountSubquery.getSelection(),
                root.get("isSavedInProducer")
        ));

        TypedQuery<ShiftTableRowDTO> typedQuery = entityManager.createQuery(criteriaQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<ShiftTableRowDTO> data = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Shift> countRoot = countQuery.from(Shift.class);

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        countQuery.select(criteriaBuilder.count(countRoot));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }
}
