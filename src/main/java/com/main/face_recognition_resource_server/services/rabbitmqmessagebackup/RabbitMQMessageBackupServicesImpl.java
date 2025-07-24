package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessage;
import com.main.face_recognition_resource_server.constants.MessageStatus;
import com.main.face_recognition_resource_server.domains.RabbitMQMessageBackup;
import com.main.face_recognition_resource_server.repositories.RabbitMQMessageBackupRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
public class RabbitMQMessageBackupServicesImpl implements RabbitMQMessageBackupServices {
    private final RabbitMQMessageBackupRepository rabbitMQMessageBackupRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RabbitMQMessageBackupServicesImpl(RabbitMQMessageBackupRepository rabbitMQMessageBackupRepository) {
        this.rabbitMQMessageBackupRepository = rabbitMQMessageBackupRepository;
    }

    @Override
    @Transactional
    @Retryable(retryFor = {SQLException.class, DataIntegrityViolationException.class})
    public ShiftMessage backupAndReturnMessage(ShiftMessage shiftMessage) throws SQLException, JsonProcessingException {
        UUID id = UUID.randomUUID();
        if (rabbitMQMessageBackupRepository.existsById(id)) {
            throw new SQLException();
        }
        shiftMessage.setMessageBackupId(id);
        String shiftMessageString = objectMapper.writeValueAsString(shiftMessage);
        RabbitMQMessageBackup backup = RabbitMQMessageBackup.builder()
                .id(id)
                .message(shiftMessageString)
                .messageStatus(MessageStatus.SENT)
                .build();

        rabbitMQMessageBackupRepository.saveAndFlush(backup);
        return shiftMessage;
    }

    @Override
    public void backupAndReturnMessage(RabbitMQMessageBackup rabbitMQMessageBackup) {
        rabbitMQMessageBackupRepository.saveAndFlush(rabbitMQMessageBackup);
    }
}
