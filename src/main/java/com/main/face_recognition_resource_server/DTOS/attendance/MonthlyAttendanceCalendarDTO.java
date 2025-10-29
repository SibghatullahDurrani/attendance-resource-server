package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonthlyAttendanceCalendarDTO {
    private List<CalendarAttendanceDataDTO> data;
    private int maxDays;
    private String firstDayOfTheMonth;
    private String lastDayOfTheMonth;
    private int lastDateOfPreviousMonth;
}
