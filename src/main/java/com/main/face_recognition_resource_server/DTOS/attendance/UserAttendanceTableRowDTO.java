package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@Data
public class UserAttendanceTableRowDTO {
    private Long id;
    private Long date;
    private AttendanceStatus status;
    private List<Long> checkIns;
    private List<Long> checkOuts;
    private byte[] score;

    public UserAttendanceTableRowDTO(Long id, Instant date, AttendanceStatus status) {
        this.id = id;
        this.date = date.toEpochMilli();
        this.status = status;
    }

    public void setCheckIns(List<Long> checkIns) {
        this.checkIns = checkIns.stream().sorted().toList();
    }

    public void setCheckOuts(List<Long> checkOuts) {
        this.checkOuts = checkOuts.stream().sorted().toList();
    }
}
