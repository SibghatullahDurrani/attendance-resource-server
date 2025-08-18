package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.DailyUserAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.OrganizationUserAttendanceDTO;
import com.main.face_recognition_resource_server.domains.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class AttendanceCriteriaRepositoryImpl implements AttendanceCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<DailyUserAttendanceDTO> getDailyUserAttendances(Specification<Attendance> specification, Pageable pageable) throws IOException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<DailyUserAttendanceDTO> criteriaQuery = criteriaBuilder.createQuery(DailyUserAttendanceDTO.class);
        Root<Attendance> root = criteriaQuery.from(Attendance.class);

        Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);

        Subquery<Date> firstCheckInSubQuery = criteriaQuery.subquery(Date.class);
        Root<CheckIn> checkInRoot = firstCheckInSubQuery.from(CheckIn.class);
        Expression<Date> checkInDatePath = checkInRoot.get("date").as(Date.class);
        firstCheckInSubQuery.select(criteriaBuilder.least(checkInDatePath));
        firstCheckInSubQuery.where(criteriaBuilder.equal(checkInRoot.get("attendance"), root));

        Subquery<Date> latestCheckOutSubquery = criteriaQuery.subquery(Date.class);
        Root<CheckOut> checkOutRoot = latestCheckOutSubquery.from(CheckOut.class);
        Expression<Date> checkOutDatePath = checkOutRoot.get("date").as(Date.class);
        latestCheckOutSubquery.select(criteriaBuilder.greatest(checkOutDatePath));
        latestCheckOutSubquery.where(criteriaBuilder.equal(checkOutRoot.get("attendance"), root));

        Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);

        Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
        Expression<Date> firstCheckInExpr = firstCheckInSubQuery.getSelection();
        Expression<Date> latestCheckOutExpr = latestCheckOutSubquery.getSelection();

        Expression<Object> nullsLastExpression = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(firstCheckInExpr), 1)
                .otherwise(0);

        Expression<Date> latestActivityDate = criteriaBuilder.function(
                "GREATEST",
                Date.class,
                criteriaBuilder.coalesce(firstCheckInExpr, criteriaBuilder.literal(new Date(0))),
                criteriaBuilder.coalesce(latestCheckOutExpr, criteriaBuilder.literal(new Date(0)))
        );

        criteriaQuery.orderBy(
                criteriaBuilder.asc(nullsLastExpression),
                criteriaBuilder.desc(latestActivityDate)
        );
        criteriaQuery.select(criteriaBuilder.construct(
                DailyUserAttendanceDTO.class,
                attendanceUserJoin.get("id"),
                attendanceUserJoin.get("firstName"),
                attendanceUserJoin.get("secondName"),
                root.get("status"),
                root.get("currentAttendanceStatus"),
                attendanceUserJoin.get("designation"),
                attendanceUserDepartmentJoin.get("departmentName"),
                firstCheckInSubQuery.getSelection(),
                latestCheckOutSubquery.getSelection()
        ));

        TypedQuery<DailyUserAttendanceDTO> typedQuery = entityManager.createQuery(criteriaQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<DailyUserAttendanceDTO> data = typedQuery.getResultList();

        for (DailyUserAttendanceDTO attendance : data) {
            String sourceImageURI = "SourceFaces/%s%s".formatted(attendance.getUserId(), ".jpg");
            Path sourceImagePath = Paths.get(sourceImageURI);
            if (Files.exists(sourceImagePath)) {
                byte[] sourceImage = Files.readAllBytes(sourceImagePath);
                attendance.setSourceImage(sourceImage);
            }
        }

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

    @Override
    public Page<OrganizationUserAttendanceDTO> getOrganizationMonthlyUserAttendances(Specification<Attendance> specification, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<OrganizationUserAttendanceDTO> criteriaQuery = criteriaBuilder.createQuery(OrganizationUserAttendanceDTO.class);
        Root<Attendance> attendanceRoot = criteriaQuery.from(Attendance.class);

        Join<Attendance, User> attendanceUserJoin = attendanceRoot.join("user", JoinType.INNER);
        Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);

        Predicate predicate = specification.toPredicate(attendanceRoot, criteriaQuery, criteriaBuilder);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }

        criteriaQuery.groupBy(
                attendanceUserJoin.get("id"),
                attendanceUserJoin.get("firstName"),
                attendanceUserJoin.get("secondName"),
                attendanceUserDepartmentJoin.get("departmentName"),
                attendanceUserJoin.get("designation")
        );

        criteriaQuery.orderBy(criteriaBuilder.asc(attendanceUserJoin.get("firstName")));

        criteriaQuery.select(criteriaBuilder.construct(
                OrganizationUserAttendanceDTO.class,
                attendanceUserJoin.get("id"),
                attendanceUserJoin.get("firstName"),
                attendanceUserJoin.get("secondName"),
                attendanceUserDepartmentJoin.get("departmentName"),
                attendanceUserJoin.get("designation"),
                criteriaBuilder.count(
                        criteriaBuilder.selectCase()
                                .when(criteriaBuilder.or(
                                        criteriaBuilder.equal(attendanceRoot.get("status"), "ON_TIME"),
                                        criteriaBuilder.equal(attendanceRoot.get("status"), "LATE")
                                ), 1)
                ),
                criteriaBuilder.count(
                        criteriaBuilder.selectCase()
                                .when(criteriaBuilder.equal(attendanceRoot.get("status"), "ON_TIME"), 1)
                ),
                criteriaBuilder.count(
                        criteriaBuilder.selectCase()
                                .when(criteriaBuilder.equal(attendanceRoot.get("status"), "ABSENT"), 1)
                ),
                criteriaBuilder.count(
                        criteriaBuilder.selectCase()
                                .when(criteriaBuilder.equal(attendanceRoot.get("status"), "ON_LEAVE"), 1)
                ),
                criteriaBuilder.count(
                        criteriaBuilder.selectCase()
                                .when(criteriaBuilder.equal(attendanceRoot.get("status"), "LATE"), 1)
                )
        ));

        TypedQuery<OrganizationUserAttendanceDTO> typedQuery = entityManager.createQuery(criteriaQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<OrganizationUserAttendanceDTO> data = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Attendance> countRoot = countQuery.from(Attendance.class);
        Join<Attendance, User> attendanceUserCountJoin = countRoot.join("user", JoinType.INNER);

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        countQuery.select(criteriaBuilder.countDistinct(attendanceUserCountJoin));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }
}
