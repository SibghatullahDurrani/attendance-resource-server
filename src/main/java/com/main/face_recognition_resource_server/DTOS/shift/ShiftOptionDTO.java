package com.main.face_recognition_resource_server.DTOS.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShiftOptionDTO {
    private Long id;
    private String shiftName;
    private boolean isDefault;
}
