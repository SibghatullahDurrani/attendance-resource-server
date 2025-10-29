package com.main.face_recognition_resource_server;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.constants.organization.OrganizationType;
import com.main.face_recognition_resource_server.constants.shift.ShiftMode;
import com.main.face_recognition_resource_server.constants.shift.WorkingDays;
import com.main.face_recognition_resource_server.constants.user.UserRole;
import com.main.face_recognition_resource_server.domains.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;

public class TestDataHelper {
    public static OrganizationPolicies createOrganizationPolicies() {
        return OrganizationPolicies.builder()
                .lateAttendanceToleranceTimeInMinutes(15)
                .sickLeavesAllowed(10)
                .annualLeavesAllowed(20)
                .build();
    }

    public static Organization createOrganization(OrganizationPolicies policies) {
        return Organization.builder()
                .organizationName("Test Organization")
                .organizationType(OrganizationType.SCHOOL)
                .organizationPolicies(policies)
                .timeZone("Asia/Karachi")
                .build();
    }

    public static Shift createShift(Organization org) {
        return Shift.builder()
                .name("Morning Shift")
                .checkInTime("09:00")
                .checkOutTime("17:00")
                .organization(org)
                .isDefault(true)
                .isSavedInProducer(true)
                .lastSavedInProducerDate(new Date())
                .workingDays(Set.of(
                        WorkingDays.MONDAY,
                        WorkingDays.TUESDAY,
                        WorkingDays.WEDNESDAY,
                        WorkingDays.THURSDAY,
                        WorkingDays.FRIDAY
                ))
                .build();
    }

    public static Department createDepartment(Organization org) {
        return Department.builder()
                .departmentName("Test Department")
                .organization(org)
                .build();
    }

    public static UserShiftSetting createUserShiftSetting() {
        return UserShiftSetting.builder()
                .shiftMode(ShiftMode.PERMANENT)
                .startDate(new Date())
                .build();
    }

    public static User createUser(Department dept, Shift shift, UserShiftSetting setting, String username) {
        return User.builder()
                .firstName("John")
                .secondName("Doe")
                .username(username)
                .password("password")
                .profilePictureName("profile.jpg")
                .sourceFacePictureName("face.jpg")
                .role(UserRole.ROLE_USER)
                .identificationNumber("1234567890123")
                .phoneNumber("123-456-7890")
                .email("example@example.com")
                .designation("Developer")
                .department(dept)
                .remainingSickLeaves(10)
                .remainingAnnualLeaves(20)
                .userShift(shift)
                .userShiftSetting(setting)
                .isSavedInProducer(true)
                .lastSavedInProducerDate(new Date())
                .build();
    }

    public static Attendance createAttendance(User user, Instant date) {
        return Attendance.builder()
                .user(user)
                .date(date)
                .status(AttendanceStatus.ON_TIME)
                .currentAttendanceStatus(AttendanceType.CHECK_IN)
                .build();
    }

    public static CheckIn createCheckIn(Attendance attendance, Instant date) {
        return CheckIn.builder()
                .attendance(attendance)
                .date(date)
                .faceImageName("checkInFace.jpg")
                .fullImageName("checkInFull.jpg")
                .build();
    }

    public static CheckOut createCheckOut(Attendance attendance, Instant date) {
        return CheckOut.builder()
                .attendance(attendance)
                .date(date)
                .faceImageName("checkOutFace.jpg")
                .fullImageName("checkOutFull.jpg")
                .build();
    }

    public static Instant[] getStartAndEndDateInstant() {
        ZoneId zone = ZoneId.of("Asia/Karachi");
        LocalDate localDate = LocalDate.now(zone);
        ZonedDateTime startOfDay = localDate.atStartOfDay(zone);
        ZonedDateTime endOfDay = localDate.plusDays(1).atStartOfDay(zone);
        return new Instant[]{startOfDay.toInstant(), endOfDay.toInstant()};
    }
}
