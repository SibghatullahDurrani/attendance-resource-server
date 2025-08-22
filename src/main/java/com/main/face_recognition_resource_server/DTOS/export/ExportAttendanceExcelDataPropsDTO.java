package com.main.face_recognition_resource_server.DTOS.export;

import com.main.face_recognition_resource_server.constants.ExportMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExportAttendanceExcelDataPropsDTO {
    private Long fromDate;
    private Long toDate;
    private ExportMode exportMode;
    private List<Long> userIds;
    private List<Long> departmentIds;
}
