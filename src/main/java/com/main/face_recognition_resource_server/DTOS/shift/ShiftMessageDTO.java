package com.main.face_recognition_resource_server.DTOS.shift;

import com.main.face_recognition_resource_server.constants.ShiftMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShiftMessageDTO {
    private ShiftMessageType shiftMessageType;
    private Object payload;
}
