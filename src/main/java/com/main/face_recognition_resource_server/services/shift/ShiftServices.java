package com.main.face_recognition_resource_server.services.shift;

import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;
import com.main.face_recognition_resource_server.DTOS.shift.ShiftTableRowDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.sql.SQLException;

public interface ShiftServices {
    void registerShift(RegisterShiftDTO registerShiftDTO, Long organizationId) throws SQLException;

    Page<ShiftTableRowDTO> getShiftsPage(Long organizationId, String name, String checkInTime, String checkOutTime, PageRequest pageRequest);
}
