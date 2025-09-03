package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Data
public class UserAttendanceTableDTO {
    private Long id;
    private Long date;
    private AttendanceStatus status;
    private List<Long> checkIns;
    private List<Long> checkOuts;
    private byte[] score;

    public UserAttendanceTableDTO(Long id, Date date, AttendanceStatus status) {
        this.id = id;
        this.date = date.getTime();
        this.status = status;
    }

    public void setCheckIns(List<Long> checkIns) {
        this.checkIns = checkIns.stream().sorted().toList();
    }

    public void setCheckOuts(List<Long> checkOuts) {
        this.checkOuts = checkOuts.stream().sorted().toList();
    }
}
