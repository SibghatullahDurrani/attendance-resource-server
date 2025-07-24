package com.main.face_recognition_resource_server.DTOS.shift;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterShiftDTO {
    private String name;
    private String checkInTime;
    private String checkOutTime;
}
