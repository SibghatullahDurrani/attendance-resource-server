package com.main.face_recognition_resource_server.DTOS.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepartmentsTableRecordDTO {
  private String departmentName;
  private int usersCount;
}
