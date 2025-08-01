package com.main.face_recognition_resource_server.DTOS.user;

import com.main.face_recognition_resource_server.constants.UserMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserMessageDTO {
    private UserMessageType userMessageType;
    private Object payload;
}
