package com.main.face_recognition_resource_server.services.shift;

import com.main.face_recognition_resource_server.DTOS.shift.RegisterShiftDTO;

import java.sql.SQLException;

public interface ShiftServices {
    void registerShift(RegisterShiftDTO registerShiftDTO, Long organizationId) throws SQLException;
}
