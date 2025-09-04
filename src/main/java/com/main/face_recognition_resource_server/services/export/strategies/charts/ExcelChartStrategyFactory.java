package com.main.face_recognition_resource_server.services.export.strategies.charts;

import com.main.face_recognition_resource_server.DTOS.export.ExcelChartDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelChartStrategyFactory {
    private final Map<ExcelChartStrategyType, ExcelChartStrategy<? extends ExcelChartDTO>> excelChartStrategiesMap = new EnumMap<>(ExcelChartStrategyType.class);

    public ExcelChartStrategyFactory(List<ExcelChartStrategy<? extends ExcelChartDTO>> excelChartStrategies) {
        for (ExcelChartStrategy<? extends ExcelChartDTO> excelChartStrategy : excelChartStrategies) {
            ExcelChartStrategyKey key = excelChartStrategy.getClass().getAnnotation(ExcelChartStrategyKey.class);
            if (key != null) {
                excelChartStrategiesMap.put(key.value(), excelChartStrategy);
            }
        }
    }

    public <T extends ExcelChartDTO> ExcelChartStrategy<T> getExcelChartStrategy(ExcelChartStrategyType excelChartStrategyType) {
        ExcelChartStrategy<? extends ExcelChartDTO> raw = excelChartStrategiesMap.get(excelChartStrategyType);
        if (raw == null) {
            throw new IllegalArgumentException("No strategy found for type " + excelChartStrategyType);
        }

        if (!excelChartStrategyType.getExcelChartDtoType().equals(raw.getExcelChartDTOClass())) {
            throw new IllegalArgumentException("Wrong excel chart strategy type.");
        }

        @SuppressWarnings("unchecked")
        ExcelChartStrategy<T> result = (ExcelChartStrategy<T>) raw;
        return result;
    }
}
