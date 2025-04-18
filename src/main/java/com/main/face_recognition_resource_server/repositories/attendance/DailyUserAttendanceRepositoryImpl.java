package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyUserAttendanceDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.Department;
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

public class DailyUserAttendanceRepositoryImpl implements DailyUserAttendanceRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<DailyUserAttendanceDTO> getDailyUserAttendances(Specification<Attendance> specification, Pageable pageable) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    CriteriaQuery<DailyUserAttendanceDTO> criteriaQuery = criteriaBuilder.createQuery(DailyUserAttendanceDTO.class);
    Root<Attendance> root = criteriaQuery.from(Attendance.class);

    Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);
    Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);

    Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
    if (predicate != null) {
      criteriaQuery.where(predicate);
    }

    criteriaQuery.select(criteriaBuilder.construct(
            DailyUserAttendanceDTO.class,
            attendanceUserJoin.get("firstName"),
            attendanceUserJoin.get("secondName"),
            root.get("status"),
            root.get("currentAttendanceStatus"),
            attendanceUserDepartmentJoin.get("departmentName")
    ));

    TypedQuery<DailyUserAttendanceDTO> typedQuery = entityManager.createQuery(criteriaQuery)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

    List<DailyUserAttendanceDTO> data = typedQuery.getResultList();

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<Attendance> countRoot = countQuery.from(Attendance.class);

    Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
    if (countPredicate != null) {
      countQuery.where(countPredicate);
    }

    countQuery.select(criteriaBuilder.count(countRoot));
    Long total = entityManager.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(data, pageable, total);
  }
}
