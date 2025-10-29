package com.main.face_recognition_resource_server.repositories.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.domains.*;
import com.main.face_recognition_resource_server.projections.attendance.AttendanceReportUserProjection;
import com.main.face_recognition_resource_server.projections.attendance.CheckInCheckOutReportAttendanceProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

@Slf4j
public class AttendanceCriteriaRepositoryImpl implements AttendanceCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<DailyUserAttendanceDTO> getDailyUserAttendances(Specification<Attendance> specification, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<DailyUserAttendanceDTO> criteriaQuery = criteriaBuilder.createQuery(DailyUserAttendanceDTO.class);
        Root<Attendance> root = criteriaQuery.from(Attendance.class);

        Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);

        Subquery<Instant> firstCheckInSubQuery = criteriaQuery.subquery(Instant.class);
        Root<CheckIn> checkInRoot = firstCheckInSubQuery.from(CheckIn.class);
        firstCheckInSubQuery.select(criteriaBuilder.least(checkInRoot.get("date").as(Instant.class)));
        firstCheckInSubQuery.where(criteriaBuilder.equal(checkInRoot.get("attendance"), root));

        Subquery<Instant> latestCheckOutSubquery = criteriaQuery.subquery(Instant.class);
        Root<CheckOut> checkOutRoot = latestCheckOutSubquery.from(CheckOut.class);
        latestCheckOutSubquery.select(criteriaBuilder.greatest(checkOutRoot.get("date").as(Instant.class)));
        latestCheckOutSubquery.where(criteriaBuilder.equal(checkOutRoot.get("attendance"), root));

        Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);

        Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
        Expression<Instant> firstCheckInExpr = firstCheckInSubQuery.getSelection();
        Expression<Instant> latestCheckOutExpr = latestCheckOutSubquery.getSelection();

        Expression<Object> nullsLastExpression = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(firstCheckInExpr), 1)
                .otherwise(0);
//
        Instant defaultDate = Instant.parse("1970-01-01T00:00:00Z");
        Expression<Instant> latestActivityDate = criteriaBuilder.function(
                "GREATEST",
                Instant.class,
                criteriaBuilder.coalesce(firstCheckInExpr, criteriaBuilder.literal(defaultDate)),
                criteriaBuilder.coalesce(latestCheckOutExpr, criteriaBuilder.literal(defaultDate))
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
                firstCheckInExpr,
                latestCheckOutExpr
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

    @Override
    public Page<CheckInCheckOutReportRecordDTO> getCheckInCheckOutReportPage(Specification<Attendance> specification, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        List<AttendanceReportUserProjection> users = getAttendanceReportUsers(specification, pageable, criteriaBuilder);

        if (users.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<CheckInCheckOutReportRecordDTO> result = getCheckInCheckOutReportAttendancesResult(specification, criteriaBuilder, users);

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Attendance> countRoot = countQuery.from(Attendance.class);
        Join<Attendance, User> countUserJoin = countRoot.join("user", JoinType.INNER);

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        countQuery.select(criteriaBuilder.countDistinct(countUserJoin.get("id")));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Page<AttendanceAnalyticsReportRecordDTO> getAttendanceAnalyticsReportPage(Specification<Attendance> specification, Pageable pageable) {
        return null;
    }

    private List<AttendanceReportUserProjection> getAttendanceReportUsers(Specification<Attendance> specification, Pageable pageable, CriteriaBuilder criteriaBuilder) {
        CriteriaQuery<AttendanceReportUserProjection> userQuery = criteriaBuilder.createQuery(AttendanceReportUserProjection.class);
        Root<Attendance> root = userQuery.from(Attendance.class);
        Join<Attendance, User> userJoin = root.join("user", JoinType.INNER);
        Join<User, Department> departmentJoin = userJoin.join("department", JoinType.INNER);

        Predicate userPredicate = specification.toPredicate(root, userQuery, criteriaBuilder);
        if (userPredicate != null) {
            userQuery.where(userPredicate);
        }

        userQuery.groupBy(
                userJoin.get("id"),
                userJoin.get("firstName"),
                userJoin.get("secondName"),
                departmentJoin.get("departmentName"),
                userJoin.get("designation")
        );

        userQuery.orderBy(criteriaBuilder.asc(userJoin.get("firstName")));
        userQuery.distinct(true);

        userQuery.multiselect(
                userJoin.get("id"),
                userJoin.get("firstName"),
                userJoin.get("secondName"),
                departmentJoin.get("departmentName"),
                userJoin.get("designation")
        );

        TypedQuery<AttendanceReportUserProjection> userTypedQuery = entityManager.createQuery(userQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        return userTypedQuery.getResultList();
    }

    private List<CheckInCheckOutReportRecordDTO> getCheckInCheckOutReportAttendancesResult(Specification<Attendance> specification, CriteriaBuilder criteriaBuilder, List<AttendanceReportUserProjection> users) {
        CriteriaQuery<CheckInCheckOutReportAttendanceProjection> attendanceQuery = criteriaBuilder.createQuery(CheckInCheckOutReportAttendanceProjection.class);
        Root<Attendance> attendanceRoot = attendanceQuery.from(Attendance.class);
        Join<Attendance, User> attendanceUserJoin = attendanceRoot.join("user", JoinType.INNER);

        Subquery<Instant> firstCheckInSubQuery = attendanceQuery.subquery(Instant.class);
        Root<CheckIn> checkInRoot = firstCheckInSubQuery.from(CheckIn.class);
        firstCheckInSubQuery.select(criteriaBuilder.least(checkInRoot.get("date").as(Instant.class)));
        firstCheckInSubQuery.where(criteriaBuilder.equal(checkInRoot.get("attendance"), attendanceRoot));

        Subquery<Instant> latestCheckOutSubquery = attendanceQuery.subquery(Instant.class);
        Root<CheckOut> checkOutRoot = latestCheckOutSubquery.from(CheckOut.class);
        latestCheckOutSubquery.select(criteriaBuilder.greatest(checkOutRoot.get("date").as(Instant.class)));
        latestCheckOutSubquery.where(criteriaBuilder.equal(checkOutRoot.get("attendance"), attendanceRoot));

        Predicate attendancePredicate = specification.toPredicate(attendanceRoot, attendanceQuery, criteriaBuilder);

        List<Long> userIds = users.stream()
                .map(AttendanceReportUserProjection::getUserId)
                .collect(java.util.stream.Collectors.toList());
        Predicate userIdPredicate = attendanceUserJoin.get("id").in(userIds);

        if (attendancePredicate != null) {
            attendanceQuery.where(criteriaBuilder.and(attendancePredicate, userIdPredicate));
        } else {
            attendanceQuery.where(userIdPredicate);
        }

        attendanceQuery.orderBy(criteriaBuilder.asc(attendanceRoot.get("date")));

        attendanceQuery.multiselect(
                attendanceUserJoin.get("id"),
                attendanceRoot.get("id"),
                attendanceRoot.get("date"),
                attendanceRoot.get("status"),
                firstCheckInSubQuery.getSelection(),
                latestCheckOutSubquery.getSelection()
        );

        List<CheckInCheckOutReportAttendanceProjection> attendanceData = entityManager.createQuery(attendanceQuery).getResultList();

        return users.stream()
                .map(user -> {
                    Long userId = user.getUserId();
                    String firstName = user.getFirstName();
                    String secondName = user.getSecondName();
                    String division = user.getDivisionName();
                    String designation = user.getDesignation();

                    List<CheckInCheckOutReportAttendance> reportAttendances = attendanceData.stream()
                            .filter(attendanceProjection -> userId.equals(attendanceProjection.getUserId()))
                            .map(attendanceProjection -> {
                                Long attendanceId = attendanceProjection.getAttendanceId();
                                Instant attendanceDate = attendanceProjection.getDate();
                                AttendanceStatus status = attendanceProjection.getAttendanceStatus();
                                Instant firstCheckIn = attendanceProjection.getFirstCheckIn();
                                Instant lastCheckOut = attendanceProjection.getLastCheckOut();

                                return CheckInCheckOutReportAttendance.builder()
                                        .date(attendanceDate.toEpochMilli())
                                        .checkIn(firstCheckIn != null ? firstCheckIn.toEpochMilli() : null)
                                        .checkOut(lastCheckOut != null ? lastCheckOut.toEpochMilli() : null)
                                        .attendanceStatus(status)
                                        .build();
                            })
                            .sorted(java.util.Comparator.comparing(CheckInCheckOutReportAttendance::getDate))
                            .collect(java.util.stream.Collectors.toList());

                    return CheckInCheckOutReportRecordDTO.builder()
                            .firstName(firstName)
                            .secondName(secondName)
                            .division(division)
                            .designation(designation)
                            .reportAttendances(reportAttendances)
                            .build();
                })
                .toList();
    }
}
