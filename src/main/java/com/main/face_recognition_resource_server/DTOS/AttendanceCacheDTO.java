package com.main.face_recognition_resource_server.DTOS;

import com.main.face_recognition_resource_server.constants.CameraTypes;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AttendanceCacheDTO {
  private Long userId;
  private Date time;
  private CameraTypes cameraType;
}
