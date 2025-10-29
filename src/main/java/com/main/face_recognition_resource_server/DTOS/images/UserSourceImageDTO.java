package com.main.face_recognition_resource_server.DTOS.images;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserSourceImageDTO {
    private Long userId;
    private byte[] image;
}
