package com.main.face_recognition_resource_server.converters;

import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.main.face_recognition_resource_server.domains.Camera;

public class CameraToCameraDTOConvertor {
  public static CameraDTO convert(Camera camera) {
    return CameraDTO.builder()
            .ipAddress(camera.getIpAddress())
            .port(camera.getPort())
            .username(camera.getUsername())
            .password(camera.getPassword())
            .type(camera.getType())
            .build();
  }

  public static Camera convert(CameraDTO cameraDTO) {
    return Camera.builder()
            .ipAddress(cameraDTO.getIpAddress())
            .port(cameraDTO.getPort())
            .username(cameraDTO.getUsername())
            .password(cameraDTO.getPassword())
            .type(cameraDTO.getType())
            .build();
  }
}
