package com.main.face_recognition_resource_server.DTOS.shift;

import com.main.face_recognition_resource_server.constants.ShiftMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShiftMessage {
    private ShiftMessageType shiftMessageType;
    private Object payload;
    private UUID messageBackupId;
}
