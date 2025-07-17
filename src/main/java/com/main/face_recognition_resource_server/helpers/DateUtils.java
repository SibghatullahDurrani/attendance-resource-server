package com.main.face_recognition_resource_server.helpers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

  public static Date[] getStartAndEndDateOfDate(int year, int month, int day) {
    Date startDate = new GregorianCalendar(year, month, day, 0, 0).getTime();
    Calendar endCalendar = GregorianCalendar.getInstance();
    endCalendar.set(Calendar.YEAR, year);
    endCalendar.set(Calendar.MONTH, month);
    endCalendar.set(Calendar.DAY_OF_MONTH, day);
    endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
    endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
    endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
    Date endDate = endCalendar.getTime();
    return new Date[]{startDate, endDate};
  }

  public static Date[] getStartAndEndDateOfToday() {
    Calendar calendarStart = GregorianCalendar.getInstance();
    Calendar calendarEnd = GregorianCalendar.getInstance();
    calendarStart.set(Calendar.HOUR_OF_DAY, 0);
    calendarStart.set(Calendar.MINUTE, 0);
    calendarStart.set(Calendar.SECOND, 0);
    calendarStart.set(Calendar.MILLISECOND, 0);

    calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
    calendarEnd.set(Calendar.MINUTE, 59);
    calendarEnd.set(Calendar.SECOND, 59);
    calendarEnd.set(Calendar.MILLISECOND, 59);
    Date dateStart = calendarStart.getTime();
    Date dateEnd = calendarEnd.getTime();
    return new Date[]{dateStart, dateEnd};
  }

  public static Date getDateOfToday() {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
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
}