package com.main.face_recognition_resource_server.services.shift;

import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftOptionDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import com.main.face_recognition_resource_server.domains.Shift;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface ShiftServices {
    void registerShift(RegisterShiftDTO registerShiftDTO, Long organizationId) throws SQLException;

    Page<ShiftTableRowDTO> getShiftsPage(Long organizationId, String name, String checkInTime, String checkOutTime, PageRequest pageRequest);

    void handleShiftAcknowledgementMessage(Message message, Channel channel) throws IOException;

    List<ShiftOptionDTO> getShiftOptions(Long organizationId);

    Shift getShiftById(Long shiftId);
}
