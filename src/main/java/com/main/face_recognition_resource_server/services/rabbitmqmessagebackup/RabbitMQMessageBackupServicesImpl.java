package com.main.face_recognition_resource_server.services.rabbitmqmessagebackup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessageDTO;
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
    public UUID backupMessageAndReturnId(ShiftMessageDTO shiftMessageDTO) throws SQLException, JsonProcessingException {
        UUID id = UUID.randomUUID();
        if (rabbitMQMessageBackupRepository.existsById(id)) {
            throw new SQLException();
        }
        String shiftMessageString = objectMapper.writeValueAsString(shiftMessageDTO);
        RabbitMQMessageBackup backup = RabbitMQMessageBackup.builder()
                .id(id)
                .message(shiftMessageString)
                .messageStatus(MessageStatus.SENT)
                .build();

        RabbitMQMessageBackup message = rabbitMQMessageBackupRepository.saveAndFlush(backup);
        return message.getId();
    }

    @Override
    public void backupMessage(RabbitMQMessageBackup rabbitMQMessageBackup) {
        rabbitMQMessageBackupRepository.saveAndFlush(rabbitMQMessageBackup);
    }
}
