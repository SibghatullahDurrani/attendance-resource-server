package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendancePreDataDTO {
    private Long userId;
    private String firstName;
    private String secondName;
    private String division;
    private String designation;
    private List<PreDataAttendanceDTO> attendances;

    public static AttendancePreDataDTO from(AttendanceExcelDataDTO attendanceExcelDataDTO) {
        AttendancePreDataDTO attendancePreData = AttendancePreDataDTO.builder()
                .userId(attendanceExcelDataDTO.getUserId())
                .firstName(attendanceExcelDataDTO.getFirstName())
                .secondName(attendanceExcelDataDTO.getSecondName())
                .division(attendanceExcelDataDTO.getDivision())
                .designation(attendanceExcelDataDTO.getDesignation())
                .build();
        List<PreDataAttendanceDTO> preDataAttendances = attendanceExcelDataDTO.getAttendances().stream().map(PreDataAttendanceDTO::from).toList();
        attendancePreData.setAttendances(preDataAttendances);
        return attendancePreData;
    }
}