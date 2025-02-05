package com.main.face_recognition_resource_server.helpers;

import com.main.face_recognition_resource_server.services.camera.dahua.FaceRecognitionSubscription;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionLockInstance {
  private final Object lock;
  private final FaceRecognitionSubscription subscription;
}
