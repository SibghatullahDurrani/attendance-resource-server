package com.main.face_recognition_resource_server.DTOS;

import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.constants.CameraType;

public class RegisterCameraDTO {
  private String ipAddress;
  private int port;
  private int channel;
  private String username;
  private String password;
  private CameraType type;
  private CameraStatus status;
  private Long departmentId;
}
