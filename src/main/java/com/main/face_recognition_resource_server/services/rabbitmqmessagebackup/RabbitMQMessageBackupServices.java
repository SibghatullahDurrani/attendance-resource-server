package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessage;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;

import java.sql.SQLException;

public interface RabbitMQMessageBackupServices {
    ShiftMessage backupAndReturnMessage(ShiftMessage shiftMessage) throws SQLException, JsonProcessingException;

    void backupAndReturnMessage(RabbitMQMessageBackup rabbitMQMessageBackup);
}
