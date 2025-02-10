package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import org.springframework.http.ResponseEntity;

public interface DepartmentServices {
  ResponseEntity<DepartmentDTO> getDepartmentByUsername(String username);
}
