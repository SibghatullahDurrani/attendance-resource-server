package com.main.face_recognition_resource_server.utilities;

import com.main.face_recognition_resource_server.constants.RabbitMQMessageType;

public record MessageMetadataWrapper(Object backupMessageId, RabbitMQMessageType rabbitMQMessageType) {
}
