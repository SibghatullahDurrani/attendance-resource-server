package com.main.face_recognition_resource_server.utilities;

import com.main.face_recognition_resource_server.constants.RoutingType;

import java.util.UUID;

public record MessageCorrelationMetaDataWrapper(UUID backupMessageId, RoutingType routingType) {
}
