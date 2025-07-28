package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessageDTO;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;

import java.sql.SQLException;
import java.util.UUID;

public interface RabbitMQMessageBackupServices {
    UUID backupMessageAndReturnId(ShiftMessageDTO shiftMessageDTO, Long organizationId) throws SQLException, JsonProcessingException;

    void backupMessage(RabbitMQMessageBackup rabbitMQMessageBackup);

    RabbitMQMessageBackup getBackupMessage(UUID backupMessageId);

    void resendPendingMessages() throws JsonProcessingException;

    void deleteDeliveredMessages();
}
