package com.main.face_recognition_resource_server.converters;

import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.main.face_recognition_resource_server.domains.Camera;
import org.springframework.stereotype.Component;

@Component
public class CameraToCameraDTOConvertor implements Converter<CameraDTO, Camera> {
  @Override
  public CameraDTO convert(Camera camera) {
    return CameraDTO.builder()
            .ipAddress(camera.getIpAddress())
            .port(camera.getPort())
            .username(camera.getUsername())
            .password(camera.getPassword())
            .type(camera.getType())
            .build();
  }
}
