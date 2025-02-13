package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CameraServices {
  void startInFaceRecognitionSubscription(String ipAddress, int port);

  ResponseEntity<List<DepartmentCameraDTO>> getCamerasOfDepartment(Long departmentId);
}
