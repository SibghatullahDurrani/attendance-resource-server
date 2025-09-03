package com.main.face_recognition_resource_server.services.export.strategies.sheet;

import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSheetStrategyKey {
    ExcelSheetCreationStrategyType value();
}
