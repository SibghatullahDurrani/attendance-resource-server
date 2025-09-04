package com.main.face_recognition_resource_server.services.export.strategies.export;

import com.main.face_recognition_resource_server.constants.export.AttendanceExportStrategyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportStrategyKey {
    AttendanceExportStrategyType value();
}
