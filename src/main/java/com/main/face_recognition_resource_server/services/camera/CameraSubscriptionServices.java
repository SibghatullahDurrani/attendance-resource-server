package com.main.face_recognition_resource_server.services.camera;

public interface CameraSubscriptionServices {
  void startFaceRecognitionSubscription(Long organizationId);

  void stopFaceRecognitionSubscription(Long organizationId);
}
