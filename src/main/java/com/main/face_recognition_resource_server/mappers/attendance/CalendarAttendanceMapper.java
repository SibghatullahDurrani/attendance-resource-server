package com.main.face_recognition_resource_server.mappers.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.CalendarAttendanceDataDTO;
import com.main.face_recognition_resource_server.projections.attendance.CalendarAttendanceProjection;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class CalendarAttendanceMapper {
    public static CalendarAttendanceDataDTO calendarAttendanceProjectionToCalendarAttendanceDataDTO(CalendarAttendanceProjection calendarAttendanceProjection, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        LocalDate date = calendarAttendanceProjection.getDate().atZone(zone).toLocalDate();
        return CalendarAttendanceDataDTO.builder()
                .date(date.getDayOfMonth())
                .status(calendarAttendanceProjection.getStatus())
                .build();
    }
}
