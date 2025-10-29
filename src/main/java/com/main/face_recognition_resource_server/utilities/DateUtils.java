package com.main.face_recognition_resource_server.utilities;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Slf4j
public class DateUtils {
    public static Date[] getStartAndEndDateOfMonthOfYear(int year, int month) {
        Date startDate = new GregorianCalendar(year, month, 1, 0, 0).getTime();
        Calendar endCalendar = GregorianCalendar.getInstance();
        endCalendar.set(Calendar.YEAR, year);
        endCalendar.set(Calendar.MONTH, month);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
        endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
        Date endDate = endCalendar.getTime();
        return new Date[]{startDate, endDate};
    }

    public static Instant[] getStartAndEndDateOfTimestampInTimeZone(Long timestamp, String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);
        LocalDate localDate = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate();
        ZonedDateTime startOfDay = localDate.atStartOfDay(zone);
        ZonedDateTime endOfDay = localDate.plusDays(1).atStartOfDay(zone);
        return new Instant[]{startOfDay.toInstant(), endOfDay.toInstant()};
    }

    public static Date[] getStartAndEndDateOfYear(int year) {
        Date startDate = new GregorianCalendar(year, Calendar.JANUARY, 1).getTime();
        Calendar endCalendar = GregorianCalendar.getInstance();
        endCalendar.set(Calendar.YEAR, year);
        endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        Date endDate = endCalendar.getTime();
        return new Date[]{startDate, endDate};
    }

    public static Instant getInstantOfTimestampInTimeZone(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Instant getInstantOfStartOfTodayOfTimeZone(String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zone).toLocalDate().atStartOfDay().atZone(zone);
        return zonedDateTime.toInstant();
    }

    public static Instant[] getStartAndEndDateOfYearInTimeZone(int year, String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);

        ZonedDateTime startOfYear = ZonedDateTime.of(LocalDate.of(year, 1, 1), LocalTime.MIDNIGHT, zone);

        ZonedDateTime endOfYear = ZonedDateTime.of(LocalDate.of(year, 12, 31), LocalTime.MAX, zone);

        return new Instant[]{startOfYear.toInstant(), endOfYear.toInstant()};
    }

    public static Instant[] getStartAndEndDateOfMonthOfYearInTimeZone(int month, int year, String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);

        ZonedDateTime startOfMonth = ZonedDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIDNIGHT, zone);

        LocalDate lastDayOfMonth = LocalDate.of(year, month, 1).withDayOfMonth(YearMonth.of(year, month).lengthOfMonth());
        ZonedDateTime endOfMonth = ZonedDateTime.of(lastDayOfMonth, LocalTime.MAX, zone);

        return new Instant[]{startOfMonth.toInstant(), endOfMonth.toInstant()};
    }

    public static Instant getStartDateOfToday(String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);
        LocalDate today = LocalDate.now(zone);
        return today.atStartOfDay(zone).toInstant();
    }

    public static Instant[] getStartAndEndDateOfDayOfMonthOfYearInTimeZone(int day, int month, int year, String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);
        LocalDate date = LocalDate.of(year, month, day);

        Instant start = date.atStartOfDay(zone).toInstant();

        Instant end = date.atTime(LocalTime.MIDNIGHT).atZone(zone).toInstant();

        return new Instant[]{start, end};
    }

    public static Instant[] getStartAndEndDateOfRangeOfTimestampInTimeZone(Long fromDate, Long toDate, String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);

        ZonedDateTime fromZonedDateTime = Instant.ofEpochMilli(fromDate).atZone(zone);
        ZonedDateTime toZonedDateTime = Instant.ofEpochMilli(toDate).atZone(zone);

        Instant start = fromZonedDateTime.toLocalDate().atStartOfDay(zone).toInstant();
        Instant end = toZonedDateTime.toLocalDate().atTime(LocalTime.MIDNIGHT).atZone(zone).toInstant();

        log.info("start Date: {}", start.toEpochMilli());
        log.info("end Date: {}", end.toEpochMilli());

        return new Instant[]{start, end};
    }

    public static Instant getStartDateOfTimestampInTimeZone(Long date, String timeZone) {
        ZoneId zone = ZoneId.of(timeZone);
        ZonedDateTime zonedDate = Instant.ofEpochMilli(date).atZone(zone);
        return zonedDate.toLocalDate().atStartOfDay(zone).toInstant();
    }
}