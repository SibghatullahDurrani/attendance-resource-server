package com.main.face_recognition_resource_server.DTOS.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserShiftChangeMessageDTO {
    private Long userId;
    private Long changedShiftId;
}
