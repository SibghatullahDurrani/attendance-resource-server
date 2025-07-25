package com.main.face_recognition_resource_server.DTOS.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShiftTableRowDTO {
    private Long id;
    private String name;
    private String checkInTime;
    private String checkOutTime;
    private boolean isDefault;
    private Long numberOfMembersInShift;
    private boolean isSavedInProducer;
}
