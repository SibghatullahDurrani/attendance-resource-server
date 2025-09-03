package com.main.face_recognition_resource_server.DTOS.user;

import com.main.face_recognition_resource_server.constants.user.UserRole;
import com.main.face_recognition_resource_server.constants.user.UsernameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RegisterUserDTO {
    private String firstName;
    private String secondName;
    private String password;
    private String identificationNumber;
    private String phoneNumber;
    private String email;
    private Long departmentId;
    private String designation;
    private String sourceImageBase64;
    private UsernameType usernameType;
    private UserRole role;
    private Long shiftId;
}