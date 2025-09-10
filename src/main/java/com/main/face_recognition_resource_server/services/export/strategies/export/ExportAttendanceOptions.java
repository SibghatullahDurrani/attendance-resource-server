package com.main.face_recognition_resource_server.services.export.strategies.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExportAttendanceOptions {
    private boolean includeCheckInCheckOutSheet;
    private boolean includeAttendanceSheet;
    private boolean includeGraphs;
    private boolean includeIndividualGraphs;
}
