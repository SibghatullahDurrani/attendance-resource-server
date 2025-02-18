package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.DTOS.CameraCredentialsDTO;
import com.main.face_recognition_resource_server.components.AttendanceCache;
import com.main.face_recognition_resource_server.components.BlockingQueueAttendanceCacheConsumer;
import com.main.face_recognition_resource_server.components.SubscriptionManager;
import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.services.camera.dahua.FaceRecognitionSubscription;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class CameraSubscriptionServicesImpl implements CameraSubscriptionServices {
  private final CameraRepository cameraRepository;
  private final AnnotationConfigApplicationContext applicationContext;
  private final Map<Long, SubscriptionManager> organizationSubscriptionManager = new HashMap<>();

  public CameraSubscriptionServicesImpl(CameraRepository cameraRepository, AnnotationConfigApplicationContext applicationContext) {
    this.cameraRepository = cameraRepository;
    this.applicationContext = applicationContext;
  }

  @Override
  public void startFaceRecognitionSubscription(Long organizationId) {
    List<CameraCredentialsDTO> cameras = cameraRepository.getCameraCredentialsOfOrganization(organizationId);
    BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue = new LinkedBlockingQueue<>();
    Object synchronizationLock = new Object();
    AttendanceCache residentCache = null;
    AttendanceCache nonResidentCache = null;
    SubscriptionManager subscriptionManager = new SubscriptionManager(
            attendanceCacheQueue,
            synchronizationLock,
            null
    );
    for (CameraCredentialsDTO camera : cameras) {
      if (camera.getType() == CameraType.IN && residentCache == null) {
//        residentCache = applicationContext.getBean(AttendanceCache.class, organizationId);
      } else if (camera.getType() == CameraType.OUT && nonResidentCache == null) {
//        nonResidentCache = new AttendanceCacheImpl(synchronizationLock);
      }
      FaceRecognitionSubscription faceRecognitionSubscription = new FaceRecognitionSubscription(camera, attendanceCacheQueue);
      Thread faceRecognitionThread = new Thread(faceRecognitionSubscription);
      subscriptionManager.addFaceRecognitionSubscription(camera.getId(), faceRecognitionThread);
      faceRecognitionThread.start();
      cameraRepository.setCameraStatusOfCamera(camera.getId(), CameraStatus.PERFORMING_FACE_RECOGNITION);
    }
    BlockingQueueAttendanceCacheConsumer attendanceCacheConsumer = new BlockingQueueAttendanceCacheConsumer(attendanceCacheQueue, residentCache, nonResidentCache, synchronizationLock);
    subscriptionManager.setAttendanceCacheConsumer(attendanceCacheConsumer);
    Thread attendanceCacheConsumerThread = new Thread(attendanceCacheConsumer);
    attendanceCacheConsumerThread.start();
    organizationSubscriptionManager.putIfAbsent(organizationId, subscriptionManager);
  }

  @Override
  public void stopFaceRecognitionSubscription(Long organizationId) {
    SubscriptionManager subscriptionManager = organizationSubscriptionManager.get(organizationId);
    if (subscriptionManager != null) {
      Map<Long, Thread> faceRecognitionSubscriptions = subscriptionManager.getFaceRecognitionSubscriptions();
      for (Map.Entry<Long, Thread> entry : faceRecognitionSubscriptions.entrySet()) {
        entry.getValue().interrupt();
        cameraRepository.setCameraStatusOfCamera(entry.getKey(), CameraStatus.INACTIVE);
      }
    }
  }
}
