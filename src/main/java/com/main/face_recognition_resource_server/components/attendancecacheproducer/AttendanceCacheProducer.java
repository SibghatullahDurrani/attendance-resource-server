package com.main.face_recognition_resource_server.components.attendancecacheproducer;

import com.main.face_recognition_resource_server.constants.CameraType;

import java.util.Date;

public interface AttendanceCacheProducer {
  void produceCache(Long userId, Date time, CameraType cameraType) throws InterruptedException;
}
