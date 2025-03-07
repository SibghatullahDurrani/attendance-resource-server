package com.main.face_recognition_resource_server.DTOS;

import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalendarAttendanceDataDTO {
  private int date;
  private AttendanceStatus status;

  public CalendarAttendanceDataDTO(Date date, AttendanceStatus status) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    this.date = calendar.get(Calendar.DAY_OF_MONTH);
    this.status = status;
  }
}
