package com.main.face_recognition_resource_server.DTOS.export;

import com.main.face_recognition_resource_server.constants.export.AttendanceExportMode;
import com.main.face_recognition_resource_server.constants.export.ExportType;
import com.main.face_recognition_resource_server.services.export.strategies.export.ExportAttendanceOptions;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class ExportExcelDataPropsDTO {
    private ExportType exportType;
    private AttendanceExportMode attendanceExportMode;
    private List<Long> userIds;
    private List<Long> departmentIds;
    private Long singleDate;
    private int month;
    private int year;
    private Long fromDate;
    private Long toDate;
    private boolean includeCheckInCheckOutSheet;
    private boolean includeAttendanceSheet;
    private boolean includeGraphs;
    private boolean includeIndividualGraphs;

    public ExportAttendanceOptions getExportAttendanceOptions() {
        return ExportAttendanceOptions.builder()
                .includeCheckInCheckOutSheet(includeCheckInCheckOutSheet)
                .includeAttendanceSheet(includeAttendanceSheet)
                .includeGraphs(includeGraphs)
                .includeIndividualGraphs(includeIndividualGraphs)
                .build();
    }
}
