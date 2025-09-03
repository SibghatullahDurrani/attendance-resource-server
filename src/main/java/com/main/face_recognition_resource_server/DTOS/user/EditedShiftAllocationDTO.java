package com.main.face_recognition_resource_server.DTOS.user;

import com.main.face_recognition_resource_server.constants.shift.ShiftMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EditedShiftAllocationDTO {
    private Long userId;
    private Long newShiftId;
    private ShiftMode newShiftMode;
    private Long newStartDate;
    private Long newEndDate;
}
