package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterCameraDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInDepartmentException;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInOrganizationException;
import com.main.face_recognition_resource_server.exceptions.CameraCanOnlyBelongToOneTypeException;

import java.util.List;

public interface CameraServices {
  void startInFaceRecognitionSubscription(String ipAddress, int port);

  List<DepartmentCameraDTO> getCamerasOfDepartment(Long departmentId);

  boolean cameraExistInDepartment(Long departmentId, List<Department> departments) throws CameraAlreadyExistsInDepartmentException;

  void registerCamera(RegisterCameraDTO cameraToRegister, Department department) throws CameraCanOnlyBelongToOneTypeException;

  void registerCamera(RegisterCameraDTO cameraToRegister, Organization organization) throws CameraAlreadyExistsInOrganizationException;
}
