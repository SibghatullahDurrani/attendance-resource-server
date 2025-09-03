package com.main.face_recognition_resource_server.services.export.strategies.sheet;

import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelSheetStrategyFactory {
    private final Map<ExcelSheetCreationStrategyType, ExcelSheetStrategy> excelSheetStrategies = new EnumMap<>(ExcelSheetCreationStrategyType.class);

    public ExcelSheetStrategyFactory(List<ExcelSheetStrategy> strategies) {
        for (ExcelSheetStrategy strategy : strategies) {
            ExcelSheetStrategyKey key = strategy.getClass().getAnnotation(ExcelSheetStrategyKey.class);
            if (key != null) {
                excelSheetStrategies.put(key.value(), strategy);
            }
        }
    }

    public ExcelSheetStrategy getStrategy(ExcelSheetCreationStrategyType strategyType) {
        return excelSheetStrategies.get(strategyType);
    }
}
