package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.exceptions.NoInCameraExistsException;

public interface CameraSubscriptionServices {
  void startFaceRecognitionSubscription(Long organizationId) throws NoInCameraExistsException;

  void stopFaceRecognitionSubscription(Long organizationId);
}
