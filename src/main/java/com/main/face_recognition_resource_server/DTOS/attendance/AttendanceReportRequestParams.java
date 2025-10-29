package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.attendance.ReportRequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceReportRequestParams {
    private int page;
    private int size;
    private Long singleDate;
    private Integer month;
    private Integer year;
    private Long startDate;
    private Long endDate;
    private List<Long> userIds;
    private List<Long> divisionIds;
    private ReportRequestType reportRequestType;
}
