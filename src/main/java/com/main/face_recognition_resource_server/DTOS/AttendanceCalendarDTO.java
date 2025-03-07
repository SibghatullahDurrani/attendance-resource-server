package com.main.face_recognition_resource_server.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceCalendarDTO {
  private List<CalendarAttendanceDataDTO> data;
  private int maxDays;
  private String firstDayOfTheMonth;
}
