package com.main.face_recognition_resource_server.services.shift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftCreationMessageDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftMessageDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import com.main.face_recognition_resource_server.constants.ShiftMessageType;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.Shift;
import com.main.face_recognition_resource_server.repositories.shift.ShiftRepository;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.main.face_recognition_resource_server.services.rabbitmqmessagebackup.RabbitMQMessageBackupServices;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

            ShiftMessageDTO shiftMessageDTO = rabbitMQMessageBackupServices.backupAndReturnMessage(
                    ShiftMessageDTO.builder()
                            .shiftMessageType(ShiftMessageType.CREATE_SHIFT)
                            .payload(shiftCreationMessageDTO)
                            .build()
            );
            String shiftMessageJson = mapper.writeValueAsString(shiftMessageDTO);
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

    @Override
    public Page<ShiftTableRowDTO> getShiftsPage(Long organizationId, String name, String checkInTime, String checkOutTime, PageRequest pageRequest) {
        Specification<Shift> specification = (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Shift, Organization> shiftOrganizationJoin = root.join("organization", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(shiftOrganizationJoin.get("id"), organizationId));
            if (name != null) {
                String nameLower = name.toLowerCase();
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("name")), nameLower));
            }
            if (checkInTime != null) {
                predicates.add(criteriaBuilder.equal(root.get("checkInTime"), checkInTime));
            }
            if (checkOutTime != null) {
                predicates.add(criteriaBuilder.equal(root.get("checkOutTime"), checkOutTime));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return shiftRepository.getShifts(specification, pageRequest);
    }
}
