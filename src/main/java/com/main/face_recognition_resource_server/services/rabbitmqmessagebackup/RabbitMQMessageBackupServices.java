package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessageDTO;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;

import java.sql.SQLException;

public interface RabbitMQMessageBackupServices {
    ShiftMessageDTO backupAndReturnMessage(ShiftMessageDTO shiftMessageDTO) throws SQLException, JsonProcessingException;

    void backupAndReturnMessage(RabbitMQMessageBackup rabbitMQMessageBackup);
}
