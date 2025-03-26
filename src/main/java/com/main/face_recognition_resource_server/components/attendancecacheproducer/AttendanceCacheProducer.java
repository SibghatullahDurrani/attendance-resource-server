package com.main.face_recognition_resource_server.components.attendancecacheproducer;

import com.main.face_recognition_resource_server.constants.CameraType;

import java.awt.image.BufferedImage;
import java.util.Date;

public interface AttendanceCacheProducer {
  void produceCache(Long userId, Date time, CameraType cameraType, BufferedImage fullImage, BufferedImage faceImage) throws InterruptedException;
}
