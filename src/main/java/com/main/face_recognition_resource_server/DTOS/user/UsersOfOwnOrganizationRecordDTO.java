package com.main.face_recognition_resource_server.DTOS.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsersOfOwnOrganizationRecordDTO {
    private Long userId;
    private String fullName;
    private String departmentName;
    private String designation;
    private String identificationNumber;
    private String email;
    private String phoneNumber;

    public UsersOfOwnOrganizationRecordDTO(
            Long userId,
            String firstName,
            String secondName,
            String departmentName,
            String designation,
            String identificationNumber,
            String email,
            String phoneNumber
    ) {
        this.userId = userId;
        this.fullName = firstName + " " + secondName;
        this.departmentName = departmentName;
        this.designation = designation;
        this.identificationNumber = identificationNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
