package com.main.face_recognition_resource_server.DTOS.export;

import com.main.face_recognition_resource_server.constants.export.ExportMode;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class ExportAttendanceExcelDataPropsDTO {
    private Long fromDate;
    private Long toDate;
    private ExportMode exportMode;
    private List<Long> userIds;
    private List<Long> departmentIds;
    private Long date;
}
