package com.main.face_recognition_resource_server.DTOS.camera;

import com.main.face_recognition_resource_server.constants.CameraType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CameraDTO {
  private Long id;

  private String ipAddress;

  private int port;

  private String username;

  private String password;

  private CameraType type;
}
