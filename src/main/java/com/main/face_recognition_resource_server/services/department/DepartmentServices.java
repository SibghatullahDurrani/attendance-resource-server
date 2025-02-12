package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface DepartmentServices {
  ResponseEntity<DepartmentDTO> getDepartmentByUsername(String username);

  ResponseEntity<HttpStatus> registerDepartmentAsSuperAdmin(RegisterDepartmentDTO departmentToRegister);

}
