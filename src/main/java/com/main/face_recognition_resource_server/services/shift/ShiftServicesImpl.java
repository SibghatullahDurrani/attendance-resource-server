package com.main.face_recognition_resource_server.services.shift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftCreationMessageDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessage;
import com.main.face_recognition_resource_server.constants.ShiftMessageType;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.Shift;
import com.main.face_recognition_resource_server.repositories.ShiftRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@Service
public class ShiftServicesImpl implements ShiftServices {
    private final ShiftRepository shiftRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OrganizationServices organizationServices;
    private final String CONTROL_EXCHANGE_NAME = "control.exchange";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ShiftServicesImpl.class);
    private final RabbitMQMessageBackupServices rabbitMQMessageBackupServices;

    public ShiftServicesImpl(ShiftRepository shiftRepository, RabbitTemplate rabbitTemplate, OrganizationServices organizationServices, RabbitMQMessageBackupServices rabbitMQMessageBackupServices) {
        this.shiftRepository = shiftRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.organizationServices = organizationServices;
        this.rabbitMQMessageBackupServices = rabbitMQMessageBackupServices;
    }

    @Override
    public void registerShift(RegisterShiftDTO registerShiftDTO, Long organizationId) throws SQLException {
        Shift shiftToRegister = Shift.builder()
                .checkInTime(registerShiftDTO.getCheckInTime())
                .checkOutTime(registerShiftDTO.getCheckOutTime())
                .organization(Organization.builder().id(organizationId).build())
                .build();
        shiftToRegister.setDefault(organizationServices.getShiftsCount(organizationId) <= 0);
        Shift registeredShift = shiftRepository.saveAndFlush(shiftToRegister);
        try {
            String SHIFT_CONTROL_ROUTING_KEY = "control." + organizationId + ".shift.key";
            ShiftCreationMessageDTO shiftCreationMessageDTO = ShiftCreationMessageDTO.builder()
                    .id(registeredShift.getId())
                    .name(registeredShift.getName())
                    .checkInTime(registerShiftDTO.getCheckInTime())
                    .checkOutTime(registerShiftDTO.getCheckOutTime())
                    .build();

            ShiftMessage shiftMessage = rabbitMQMessageBackupServices.backupAndReturnMessage(
                    ShiftMessage.builder()
                            .shiftMessageType(ShiftMessageType.CREATE_SHIFT)
                            .payload(shiftCreationMessageDTO)
                            .build()
            );
            String shiftMessageJson = mapper.writeValueAsString(shiftMessage);
            CorrelationData correlationData = new CorrelationData();
            correlationData.setReturned(new ReturnedMessage(
                    new Message(shiftMessageJson.getBytes(StandardCharsets.UTF_8)),
                    1,
                    "message sent",
                    CONTROL_EXCHANGE_NAME,
                    SHIFT_CONTROL_ROUTING_KEY
            ));
            rabbitTemplate.convertAndSend(CONTROL_EXCHANGE_NAME, SHIFT_CONTROL_ROUTING_KEY, shiftMessageJson, correlationData);
        } catch (JsonProcessingException e) {
            logger.error("Error Converting Message To JSON");
        } catch (AmqpException e) {
            logger.error("Error sending message");
        }
    }
}
