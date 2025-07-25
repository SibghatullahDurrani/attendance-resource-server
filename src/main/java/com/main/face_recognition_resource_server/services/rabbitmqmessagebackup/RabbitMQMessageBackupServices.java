package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessageDTO;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;

import java.sql.SQLException;
import java.util.UUID;

public interface RabbitMQMessageBackupServices {
    UUID backupAndReturnMessage(ShiftMessageDTO shiftMessageDTO) throws SQLException, JsonProcessingException;

    void backupAndReturnMessage(RabbitMQMessageBackup rabbitMQMessageBackup);
}
