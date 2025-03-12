package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.CameraType;
import lombok.Builder;
import lombok.Data;

import java.awt.image.BufferedImage;
import java.util.Date;

@Data
@Builder
public class AttendanceCacheDTO {
  private Long userId;
  private Date time;
  private CameraType cameraType;
  private BufferedImage image;
}
