package com.main.face_recognition_resource_server.services.export.strategies.export;

import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import com.main.face_recognition_resource_server.constants.export.AttendanceExportStrategyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AttendanceExportStrategyFactory {
    private final Map<AttendanceExportStrategyType, AttendanceExportStrategy<? extends ExcelChartDTO>> attendanceExportStrategyMap = new EnumMap<>(AttendanceExportStrategyType.class);

    public AttendanceExportStrategyFactory(List<AttendanceExportStrategy<? extends ExcelChartDTO>> attendanceExportStrategies) {
        for (AttendanceExportStrategy<? extends ExcelChartDTO> attendanceExportStrategy : attendanceExportStrategies) {
            ExportStrategyKey key = attendanceExportStrategy.getClass().getAnnotation(ExportStrategyKey.class);
            if (key != null) {
                attendanceExportStrategyMap.put(key.value(), attendanceExportStrategy);
            }
        }
    }

    public <T extends ExcelChartDTO> AttendanceExportStrategy<T> getAttendanceExportStrategy(AttendanceExportStrategyType attendanceExportStrategyType) {
        AttendanceExportStrategy<? extends ExcelChartDTO> raw = attendanceExportStrategyMap.get(attendanceExportStrategyType);
        if (raw == null) {
            throw new IllegalArgumentException("No strategy found for type " + attendanceExportStrategyType);
        }

        if (!attendanceExportStrategyType.getExcelChartDTOClass().equals(raw.getExcelChartDTOClass())) {
            throw new IllegalArgumentException("Wrong attendance export strategy type.");
        }

        @SuppressWarnings("unchecked")
        AttendanceExportStrategy<T> result = (AttendanceExportStrategy<T>) raw;
        return result;
    }
}
