package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.GetCameraDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterCameraDTO;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInOrganizationException;

import java.util.List;

public interface CameraServices {

  void registerCamera(RegisterCameraDTO cameraToRegister, Organization organization) throws CameraAlreadyExistsInOrganizationException;

  List<GetCameraDTO> getCamerasOfOrganization(Long organizationId);

  boolean cameraExists(String ipAddress, int port, int channel) throws CameraAlreadyExistsInOrganizationException;
}
