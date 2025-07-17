package com.main.face_recognition_resource_server.DTOS.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SendNotificationToUsersOfOrganizationDTO {
    private String title;
    private String message;
}
