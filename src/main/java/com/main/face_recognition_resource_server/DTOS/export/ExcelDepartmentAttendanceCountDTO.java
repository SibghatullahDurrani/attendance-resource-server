package com.main.face_recognition_resource_server.DTOS.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExcelDepartmentAttendanceCountDTO {
    private Date date;
    private Long onTime;
    private Long late;
    private Long absent;
    private Long onLeave;
}
