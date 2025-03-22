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
