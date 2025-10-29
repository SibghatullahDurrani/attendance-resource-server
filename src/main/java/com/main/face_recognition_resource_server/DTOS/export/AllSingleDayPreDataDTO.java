package com.main.face_recognition_resource_server.DTOS.export;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AllSingleDayPreDataDTO {
    List<AttendancePreDataDTO> attendanceData;
    List<DepartmentAttendancePieChartPreData> departmentAttendancePieChartPreData;
}
