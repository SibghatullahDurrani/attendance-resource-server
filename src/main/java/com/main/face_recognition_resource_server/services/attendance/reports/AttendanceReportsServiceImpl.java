package com.main.face_recognition_resource_server.services.attendance.reports;

import com.main.face_recognition_resource_server.DTOS.attendance.*;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatusFilter;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.constants.attendance.ReportRequestType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.User;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.mappers.attendance.CalendarAttendanceMapper;
import com.main.face_recognition_resource_server.projections.attendance.CalendarAttendanceProjection;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import com.main.face_recognition_resource_server.services.user.UserService;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.main.face_recognition_resource_server.utilities.DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone;

@Service
public class AttendanceReportsServiceImpl implements AttendanceReportsService {
    private final UserService userService;
    private final AttendanceRepository attendanceRepository;
    private final OrganizationService organizationService;

    public AttendanceReportsServiceImpl(UserService userService, AttendanceRepository attendanceRepository, OrganizationService organizationService) {
        this.userService = userService;
        this.attendanceRepository = attendanceRepository;
        this.organizationService = organizationService;
    }

    @Override
    public MonthlyAttendanceCalendarDTO getUserAttendanceCalendar(int month, int year, Long userId) throws NoStatsAvailableException {
        //TODO: Handle time zone via organization Id
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
        List<CalendarAttendanceProjection> calendarAttendanceProjections = attendanceRepository.getCalendarAttendanceProjectionOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);
        if (calendarAttendanceProjections.isEmpty()) {
            throw new NoStatsAvailableException();
        }
        LocalDate firstDay = LocalDate.of(year, month + 1, 1);
        LocalDate lastDay = YearMonth.of(year, month + 1).atEndOfMonth();

        int maxDays = lastDay.getDayOfMonth();

        List<CalendarAttendanceDataDTO> calendarAttendanceData = calendarAttendanceProjections.stream().map(projection -> CalendarAttendanceMapper.calendarAttendanceProjectionToCalendarAttendanceDataDTO(projection, timeZone)).toList();
        return MonthlyAttendanceCalendarDTO.builder().data(calendarAttendanceData).maxDays(maxDays).firstDayOfTheMonth(firstDay.getDayOfWeek().name()).lastDayOfTheMonth(lastDay.getDayOfWeek().name()).lastDateOfPreviousMonth(firstDay.minusDays(1).getDayOfMonth()).build();
    }

    @Override
    public List<UserAttendanceTableRowDTO> getMonthlyUserAttendanceTable(int month, int year, Long userId) throws NoStatsAvailableException {
        String timeZone = userService.getUserTimeZone(userId);
        Instant[] startAndEndDate = getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
        List<UserAttendanceTableRowDTO> attendanceTableRecords = attendanceRepository.getAttendanceTableRecordsOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);

        if (attendanceTableRecords.isEmpty()) {
            throw new NoStatsAvailableException();
        }

//        Long organizationId = userServices.getUserOrganizationIdByUserId(userId);
//        int lateAttendanceToleranceTimeInMillis = organizationServices.getOrganizationLateAttendanceToleranceTimePolicy(organizationId) * 60000;
//        long organizationRetakeAttendancePolicyInMillis = organizationServices.getAttendanceRetakeAttendanceInHourPolicy(organizationId) * 3600000L;
//        String organizationCheckInTime = organizationServices.getOrganizationCheckInPolicy(organizationId);
//        String[] organizationCheckInTimeSplit = organizationCheckInTime.split(":");
//        int organizationCheckInHour = Integer.parseInt(organizationCheckInTimeSplit[0]);
//        int organizationCheckInMinutes = Integer.parseInt(organizationCheckInTimeSplit[1]);
//        String organizationCheckOutTime = organizationServices.getOrganizationCheckOutPolicy(organizationId);
//        String[] organizationCheckOutTimeSplit = organizationCheckOutTime.split(":");
//        int organizationCheckOutHour = Integer.parseInt(organizationCheckOutTimeSplit[0]);
//        int organizationCheckOutMinutes = Integer.parseInt(organizationCheckOutTimeSplit[1]);
//        int checkOutToleranceTime = organizationServices.getOrganizationCheckOutToleranceTimePolicy(organizationId);
//
//        for (UserAttendanceTableDTO attendanceRecord : attendanceTableRecords) {
//            attendanceRecord.setCheckIns(checkInServices.getCheckInTimesByAttendanceId(attendanceRecord.getId()));
//            attendanceRecord.setCheckOuts(checkOutServices.getCheckOutTimesByAttendanceId(attendanceRecord.getId()));
//
//            BufferedImage scoreImage = new BufferedImage(scoreImageWidth, scoreImageHeight, BufferedImage.TYPE_INT_RGB);
//            Graphics scoreGraphics = scoreImage.createGraphics();
//
//            Calendar policyCheckIn = new GregorianCalendar();
//            Calendar policyCheckOut = new GregorianCalendar();
//
//            policyCheckIn.setTimeInMillis(attendanceRecord.getDate());
//            policyCheckOut.setTimeInMillis(attendanceRecord.getDate());
//
//            policyCheckIn.set(Calendar.HOUR_OF_DAY, organizationCheckInHour);
//            policyCheckIn.set(Calendar.MINUTE, organizationCheckInMinutes);
//
//            policyCheckOut.set(Calendar.HOUR_OF_DAY, organizationCheckOutHour);
//            policyCheckOut.set(Calendar.MINUTE, organizationCheckOutMinutes);
//
//            long policyCheckOutTimeStamp = policyCheckOut.getTime().getTime();
//            long policyCheckInTimeStamp = policyCheckIn.getTime().getTime();
//
//            long totalWorkingHoursTimeStamp = policyCheckOutTimeStamp - policyCheckInTimeStamp;
//
//            List<Long> checkIns = new ArrayList<>(attendanceRecord.getCheckIns());
//            List<Long> checkOuts = new ArrayList<>(attendanceRecord.getCheckOuts());
//
//            long checkIn = 0;
//            long checkOut = 0;
//            if (!checkIns.isEmpty()) {
//                checkIn = checkIns.removeFirst();
//            }
//            if (!checkOuts.isEmpty()) {
//                checkOut = checkOuts.removeFirst();
//            }
//            int previousPoint = 0;
//            boolean first = true;
//
//            if (checkIn == 0 && attendanceRecord.getStatus() == AttendanceStatus.ABSENT) {
//                drawRectangleTillEnd(scoreGraphics, previousPoint, red);
//            } else if (checkIn == 0 && attendanceRecord.getStatus() == AttendanceStatus.ON_LEAVE) {
//                drawRectangleTillEnd(scoreGraphics, previousPoint, cyan);
//            }
//
//            while (checkIn != 0 || checkOut != 0) {
//                if (first) {
//                    if (checkIn > policyCheckInTimeStamp + lateAttendanceToleranceTimeInMillis) {
//                        previousPoint = drawRectangle(scoreGraphics, previousPoint, policyCheckInTimeStamp, checkIn, totalWorkingHoursTimeStamp, red);
//                        if (checkIns.isEmpty()) {
//                            if (checkOut == 0) {
//                                if (checkIn + organizationRetakeAttendancePolicyInMillis > policyCheckOutTimeStamp) {
//                                    drawRectangle(scoreGraphics, previousPoint, checkIn, policyCheckOutTimeStamp, totalWorkingHoursTimeStamp, green);
//                                } else {
//                                    int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkIn + organizationRetakeAttendancePolicyInMillis, totalWorkingHoursTimeStamp, green);
//                                    previousPoint = previousPoint + newPoint;
//                                    drawRectangleTillEnd(scoreGraphics, previousPoint, red);
//                                }
//                            } else {
//                                int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkOut, totalWorkingHoursTimeStamp, green);
//                                previousPoint = previousPoint + newPoint;
//                            }
//                            checkIn = 0;
//                        } else {
//                            checkIn = checkIns.removeFirst();
//                        }
//                    } else {
//                        int newPoint = drawRectangle(scoreGraphics, previousPoint, policyCheckInTimeStamp, checkIn, totalWorkingHoursTimeStamp, green);
//                        previousPoint = previousPoint + newPoint;
//                    }
//                    first = false;
//                } else {
//                    if (checkIn != 0 && checkOut == 0) {
//                        if ((checkIn + organizationRetakeAttendancePolicyInMillis) > policyCheckOutTimeStamp) {
//                            drawRectangle(scoreGraphics, previousPoint, checkIn, policyCheckOutTimeStamp, totalWorkingHoursTimeStamp, green);
//                        } else {
//                            int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkIn + organizationRetakeAttendancePolicyInMillis, totalWorkingHoursTimeStamp, green);
//                            previousPoint = previousPoint + newPoint;
//                            drawRectangleTillEnd(scoreGraphics, previousPoint, red);
//                        }
//                        checkIn = 0;
//                    }
//                    if (checkIn == 0 && checkOut != 0) {
//                        drawRectangleTillEnd(scoreGraphics, previousPoint, red);
//                        checkOut = 0;
//                    }
//                    if (checkOut != 0) {
//                        if (checkOut > checkIn) {
//                            int newPoint = drawRectangle(scoreGraphics, previousPoint, checkIn, checkOut, totalWorkingHoursTimeStamp, green);
//                            previousPoint = previousPoint + newPoint;
//                            if (checkIns.isEmpty()) {
//                                checkIn = 0;
//                            } else {
//                                checkIn = checkIns.removeFirst();
//                            }
//                        } else {
//                            int newPoint = drawRectangle(scoreGraphics, previousPoint, checkOut, checkIn, totalWorkingHoursTimeStamp, red);
//                            previousPoint = previousPoint + newPoint;
//                            if (checkOuts.isEmpty()) {
//                                checkOut = 0;
//                            } else {
//                                checkOut = checkOuts.removeFirst();
//                            }
//                        }
//                    }
//                }
//            }
//
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            ImageIO.write(scoreImage, "jpg", byteArrayOutputStream);
//            attendanceRecord.setScore(byteArrayOutputStream.toByteArray());
//        }
        return attendanceTableRecords;
    }

//    @Override
//    public List<UserAttendanceDTO> getMonthlyUserAttendanceOverview(int month, int year, Long userId) throws NoStatsAvailableException {
//        String timeZone = userService.getUserTimeZone(userId);
//        Instant[] startAndEndDate = getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
//        List<UserAttendanceDTO> attendances = attendanceRepository.getAttendanceOverviewOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);
//
//        if (attendances.isEmpty()) {
//            throw new NoStatsAvailableException();
//        }
//
//        for (UserAttendanceDTO attendance : attendances) {
//            attendance.setCheckIns(checkInService.getCheckInTimesByAttendanceId(attendance.getId()));
//            attendance.setCheckOuts(checkOutService.getCheckOutTimesByAttendanceId(attendance.getId()));
//        }
//
//        return attendances;
//    }

//    @Override
//    public AttendanceSnapshotDTO getUserAttendanceSnapshots(int year, int month, int day, Long userId) throws NoStatsAvailableException {
//        String timeZone = userService.getUserTimeZone(userId);
//        Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfDayOfMonthOfYearInTimeZone(day, month, year, timeZone);
//        Optional<Long> attendanceId = attendanceRepository.getAttendanceIdOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);
//
//        if (attendanceId.isEmpty()) {
//            throw new NoStatsAvailableException();
//        }
//
//        AttendanceSnapshotDTO attendanceSnapshot = AttendanceSnapshotDTO.builder()
//                .attendanceStatus(attendanceRepository.getAttendanceStatusOfAttendance(attendanceId.get()).orElseThrow())
//                .dayTime(startAndEndDate[0].toEpochMilli())
//                .data(new ArrayList<>())
//                .build();
//
//        attendanceSnapshot.addAttendanceSnapshotDTOData(checkInService.getCheckInSnapshotsOfAttendance(attendanceId.get()));
//        attendanceSnapshot.addAttendanceSnapshotDTOData(checkOutService.getCheckOutSnapshotsOfAttendance(attendanceId.get()));
//
//        return attendanceSnapshot;
//    }

//    @Override
//    public Page<UserAttendanceDTO> getYearlyUserAttendanceTable(Pageable pageRequest, int year, Long userId) {
//        String timeZone = userService.getUserTimeZone(userId);
//        Instant[] startAndEndDate = getStartAndEndDateOfYearInTimeZone(year, timeZone);
//        Page<UserAttendanceDTO> attendancePage = attendanceRepository.getUserAttendancePageBetweenDate(userId, startAndEndDate[0], startAndEndDate[1], pageRequest);
//
//        return attendancePage.map(attendance -> {
//            attendance.setCheckIns(checkInService.getCheckInTimesByAttendanceId(attendance.getId()));
//            attendance.setCheckOuts(checkOutService.getCheckOutTimesByAttendanceId(attendance.getId()));
//            return attendance;
//        });
//    }

    @Override
    public List<MonthlyAttendanceCalendarRecordDTO> getUserAttendanceCalendar(int year, Long userId) {
        Instant[] startAndEndDate;
        AttendanceCountDTO attendanceCount;
        String timeZone = userService.getUserTimeZone(userId);
        List<MonthlyAttendanceCalendarRecordDTO> attendanceCalendarRecords = new ArrayList<>();
        for (int month = 0; month < 12; month++) {
            startAndEndDate = getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);
            attendanceCount = attendanceRepository.getAttendanceCountOfUserBetweenDates(startAndEndDate[0], startAndEndDate[1], userId);
            attendanceCalendarRecords.add(MonthlyAttendanceCalendarRecordDTO.builder().attendanceCount(attendanceCount).month(month).build());
        }
        return attendanceCalendarRecords;
    }

    @Override
    public Page<DailyUserAttendanceDTO> getDailyUserAttendances(Long organizationId, AttendanceType attendanceType, AttendanceStatusFilter attendanceStatus, String userName, List<Long> departmentIds, Pageable pageable) throws IOException {
        Specification<Attendance> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            String timeZone = organizationService.getOrganizationTimeZone(organizationId);
            ZoneId zoneId = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zoneId);
            LocalDateTime startOfDay = today.atStartOfDay();
            ZonedDateTime zonedStartOfDay = startOfDay.atZone(zoneId);
            Instant startOfDayInstant = zonedStartOfDay.toInstant();
            Instant endOfDayInstant = startOfDayInstant.plus(1, ChronoUnit.DAYS);

            // Use path expressions - the repository will create the actual joins
            predicates.add(criteriaBuilder.equal(root.get("user").get("department").get("organization").get("id"), organizationId));
            predicates.add(criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startOfDayInstant),
                    criteriaBuilder.lessThan(root.get("date"), endOfDayInstant)
            ));

            if (attendanceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentAttendanceStatus"), attendanceType));
            }

            if (attendanceStatus != null) {
                switch (attendanceStatus) {
                    case PRESENT -> predicates.add(criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("status"), AttendanceStatus.ON_TIME),
                            criteriaBuilder.equal(root.get("status"), AttendanceStatus.LATE)
                    ));
                    case ON_TIME -> predicates.add(criteriaBuilder.equal(root.get("status"), AttendanceStatus.ON_TIME));
                    case LATE -> predicates.add(criteriaBuilder.equal(root.get("status"), AttendanceStatus.LATE));
                    case ABSENT -> predicates.add(criteriaBuilder.equal(root.get("status"), AttendanceStatus.ABSENT));
                    case ON_LEAVE ->
                            predicates.add(criteriaBuilder.equal(root.get("status"), AttendanceStatus.ON_LEAVE));
                }
            }

            if (userName != null) {
                String fullNameLower = userName.toLowerCase();
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("firstName")), "%" + fullNameLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("secondName")), "%" + fullNameLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(
                                criteriaBuilder.concat(root.get("user").get("firstName"),
                                        criteriaBuilder.concat(" ", root.get("user").get("secondName")))), "%" + fullNameLower + "%")
                ));
            }

            if (departmentIds != null && !departmentIds.isEmpty()) {
                predicates.add(root.get("user").get("department").get("id").in(departmentIds));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return attendanceRepository.getDailyUserAttendances(specification, pageable);
    }

    @Override
    public Page<OrganizationUserAttendanceDTO> getOrganizationMonthlyUserAttendances(Pageable pageRequest, int year, int month, String fullName, Long departmentId, Long organizationId) {
        Specification<Attendance> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);
            Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);
            Join<Department, Organization> attendanceUserDepartmentOrganizationJoin = attendanceUserDepartmentJoin.join("organization", JoinType.INNER);
            String timeZone = organizationService.getOrganizationTimeZone(organizationId);
            Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(month, year, timeZone);

            if (fullName != null) {
                String fullNameLower = fullName.toLowerCase();
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(attendanceUserJoin.get("firstName")), "%" + fullNameLower + "%"), criteriaBuilder.like(criteriaBuilder.lower(attendanceUserJoin.get("secondName")), "%" + fullNameLower + "%"), criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.concat(attendanceUserJoin.get("firstName"), criteriaBuilder.concat(" ", attendanceUserJoin.get("secondName")))), "%" + fullNameLower + "%")));
            }
            if (departmentId != null) {
                predicates.add(criteriaBuilder.equal(attendanceUserDepartmentJoin.get("id"), departmentId));
            }

            predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startAndEndDate[0]), criteriaBuilder.lessThan(root.get("date"), startAndEndDate[1])));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startAndEndDate[0]));
            predicates.add(criteriaBuilder.equal(attendanceUserDepartmentOrganizationJoin.get("id"), organizationId));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return attendanceRepository.getOrganizationMonthlyUserAttendances(specification, pageRequest);
    }

    @Override
    public Page<CheckInCheckOutReportRecordDTO> getCheckInCheckOutReportPageOfOrganization(Pageable pageRequest, Long organizationId, AttendanceReportRequestParams attendanceReportRequestParams) {
        Specification<Attendance> specification = getAttendanceReportSpecification(organizationId, attendanceReportRequestParams);
        return attendanceRepository.getCheckInCheckOutReportPage(specification, pageRequest);
    }

    @Override
    public Page<AttendanceAnalyticsReportRecordDTO> getAttendanceAnalyticsReportPageOfOrganization(Pageable pageRequest, Long organizationId, AttendanceReportRequestParams params) {
        Specification<Attendance> specification = getAttendanceReportSpecification(organizationId, params);
        return attendanceRepository.getAttendanceAnalyticsReportPage(specification, pageRequest);
    }

    private Specification<Attendance> getAttendanceReportSpecification(Long organizationId, AttendanceReportRequestParams attendanceReportRequestParams) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Attendance, User> attendanceUserJoin = root.join("user", JoinType.INNER);
            Join<User, Department> attendanceUserDepartmentJoin = attendanceUserJoin.join("department", JoinType.INNER);
            Join<Department, Organization> attendanceUserDepartmentOrganizationJoin = attendanceUserDepartmentJoin.join("organization", JoinType.INNER);

            String timeZone = organizationService.getOrganizationTimeZone(organizationId);
            if (
                    attendanceReportRequestParams.getReportRequestType() == ReportRequestType.ALL_SINGLE_DAY
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.MEMBERS_SINGLE_DAY
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.DIVISIONS_SINGLE_DAY
            ) {
                Instant instant = DateUtils.getStartDateOfTimestampInTimeZone(attendanceReportRequestParams.getSingleDate(), timeZone);
                predicates.add(criteriaBuilder.equal(root.get("date"), instant));
            } else if (
                    attendanceReportRequestParams.getReportRequestType() == ReportRequestType.ALL_RANGE
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.MEMBERS_RANGE
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.DIVISIONS_RANGE

            ) {
                Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfRangeOfTimestampInTimeZone(attendanceReportRequestParams.getStartDate(), attendanceReportRequestParams.getEndDate(), timeZone);
                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startAndEndDate[0]),
                                criteriaBuilder.lessThanOrEqualTo(root.get("date"), startAndEndDate[1])
                        )
                );
            } else if (
                    attendanceReportRequestParams.getReportRequestType() == ReportRequestType.ALL_MONTH
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.MEMBERS_MONTH
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.DIVISIONS_MONTH
            ) {
                Instant[] startAndEndDate = DateUtils.getStartAndEndDateOfMonthOfYearInTimeZone(attendanceReportRequestParams.getMonth(), attendanceReportRequestParams.getYear(), timeZone);
                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startAndEndDate[0]),
                                criteriaBuilder.lessThanOrEqualTo(root.get("date"), startAndEndDate[1])
                        )
                );
            }

            if (
                    (attendanceReportRequestParams.getReportRequestType() == ReportRequestType.MEMBERS_SINGLE_DAY
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.MEMBERS_RANGE
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.MEMBERS_MONTH)
                            && !attendanceReportRequestParams.getUserIds().isEmpty()
            ) {
                predicates.add(attendanceUserJoin.get("id").in(attendanceReportRequestParams.getUserIds()));
            }
            if (
                    (attendanceReportRequestParams.getReportRequestType() == ReportRequestType.DIVISIONS_SINGLE_DAY
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.DIVISIONS_RANGE
                            || attendanceReportRequestParams.getReportRequestType() == ReportRequestType.DIVISIONS_MONTH)
                            && !attendanceReportRequestParams.getDivisionIds().isEmpty()
            ) {
                predicates.add(attendanceUserDepartmentJoin.get("id").in(attendanceReportRequestParams.getDivisionIds()));
            }

            predicates.add(criteriaBuilder.equal(attendanceUserDepartmentOrganizationJoin.get("id"), organizationId));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
