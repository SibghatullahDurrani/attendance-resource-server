package com.main.face_recognition_resource_server.DTOS.camera;

import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetCameraDTO {
  private Long id;
  private String ipAddress;
  private int port;
  private int channel;
  private CameraType type;
  private CameraStatus cameraStatus;
}
