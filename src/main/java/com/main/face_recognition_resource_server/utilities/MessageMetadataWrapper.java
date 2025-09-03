package com.main.face_recognition_resource_server.utilities;

import com.main.face_recognition_resource_server.constants.rabbitmq.RabbitMQMessageType;

public record MessageMetadataWrapper(Object backupMessageId, RabbitMQMessageType rabbitMQMessageType) {
}
