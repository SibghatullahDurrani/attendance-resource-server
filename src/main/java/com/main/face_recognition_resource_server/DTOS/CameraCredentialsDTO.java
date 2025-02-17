package com.main.face_recognition_resource_server.DTOS;

import com.main.face_recognition_resource_server.constants.CameraType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CameraCredentialsDTO {
  private Long id;
  private String ipAddress;
  private int port;
  private int channel;
  private String username;
  private String password;
  private CameraType type;
}
