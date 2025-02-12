package com.main.face_recognition_resource_server.utils;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.constants.OrganizationType;
import com.main.face_recognition_resource_server.constants.UserRole;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.User;

public class DataUtil {
  public static DepartmentDTO getDepartmentDTO() {
    return DepartmentDTO.builder()
            .id(1L)
            .departmentName("ASD")
            .build();
  }

  public static Department getDepartment(Organization organization) {
    return Department.builder().departmentName("XYZ").organization(organization).build();
  }

  public static Organization getOrganization() {
    return Organization.builder().organizationName("XYZ").organizationType(OrganizationType.OFFICE).build();
  }

  public static User getUser(Department department) {
    return User.builder()
            .firstName("XYZ")
            .secondName("XYZ")
            .password("XYZ")
            .username("XYZ")
            .role(UserRole.ROLE_USER)
            .identificationNumber("XYZ")
            .email("XYZ")
            .department(department)
            .build();
  }

  public static RegisterDepartmentDTO getRegisterDepartmentDTO() {
    return RegisterDepartmentDTO.builder()
            .departmentName("XYZ")
            .organizationId(1L)
            .build();
  }

}
