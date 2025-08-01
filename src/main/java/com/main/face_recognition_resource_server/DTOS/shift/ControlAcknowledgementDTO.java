package com.main.face_recognition_resource_server.DTOS.shift;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class ControlAcknowledgementDTO {
    private Long id;
    private Long savedAt;
}
