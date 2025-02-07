package com.main.face_recognition_resource_server.components;

import com.main.face_recognition_resource_server.enums.CameraTypes;

import java.util.Date;

public interface AttendanceCacheProducer {
  void produceCache(Long userId, Date time, CameraTypes cameraType) throws InterruptedException;
}
