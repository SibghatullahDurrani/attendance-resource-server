package com.main.face_recognition_resource_server.DTOS.notification;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class SendNotificationToUsersDTO {
    private String title;
    private String message;
    private List<Long> intendedUserIds;
}
