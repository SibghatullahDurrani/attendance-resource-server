package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.DTOS.camera.CameraCredentialsDTO;
import com.main.face_recognition_resource_server.components.attendancecache.AttendanceCacheFactory;
import com.main.face_recognition_resource_server.components.attendancecacheconsumer.AttendanceCacheConsumerFactory;
import com.main.face_recognition_resource_server.components.attendancecacheconsumer.BlockingQueueAttendanceCacheConsumer;
import com.main.face_recognition_resource_server.components.attendancecachequeuefactory.AttendanceCacheQueueFactory;
import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.exceptions.NoInCameraExistsException;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.services.camera.dahua.FaceRecognitionSubscription;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Service
public class CameraSubscriptionServicesImpl implements CameraSubscriptionServices {
  private final CameraRepository cameraRepository;
  private final AttendanceCacheFactory attendanceCacheFactory;
  private final AttendanceCacheQueueFactory attendanceCacheQueueFactory;
  private final AttendanceCacheConsumerFactory attendanceCacheConsumerFactory;
  private final Map<Long, Map<Long, Thread>> organizationIdFaceRecognitionSubscriptionsMap = new HashMap<>();

  public CameraSubscriptionServicesImpl(CameraRepository cameraRepository, AttendanceCacheFactory attendanceCacheFactory, AttendanceCacheQueueFactory attendanceCacheQueueFactory, AttendanceCacheConsumerFactory attendanceCacheConsumerFactory) {
    this.cameraRepository = cameraRepository;
    this.attendanceCacheFactory = attendanceCacheFactory;
    this.attendanceCacheQueueFactory = attendanceCacheQueueFactory;
    this.attendanceCacheConsumerFactory = attendanceCacheConsumerFactory;
  }

  @Override
  public void startFaceRecognitionSubscription(Long organizationId) throws NoInCameraExistsException {
    List<CameraCredentialsDTO> cameras = cameraRepository.getCameraCredentialsOfOrganization(organizationId);
    boolean InExists = cameras.stream().anyMatch(camera -> camera.getType() == CameraType.IN);
    boolean outExists = cameras.stream().anyMatch(camera -> camera.getType() == CameraType.OUT);
    Map<Long, Thread> cameraIdFaceSubscriptionMap = new HashMap<>();
    organizationIdFaceRecognitionSubscriptionsMap.putIfAbsent(organizationId, cameraIdFaceSubscriptionMap);
    if (!InExists) {
      throw new NoInCameraExistsException();
    }
    if (outExists) {
      attendanceCacheFactory.getNonResidentCache(organizationId);
    }
    BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue = attendanceCacheQueueFactory.getAttendanceCacheQueue(organizationId);
    BlockingQueueAttendanceCacheConsumer attendanceCacheConsumer = attendanceCacheConsumerFactory.getAttendanceCacheConsumer(organizationId);
    Thread attendanceCacheConsumerThread = new Thread(attendanceCacheConsumer);
    attendanceCacheConsumerThread.start();
    for (CameraCredentialsDTO camera : cameras) {
      FaceRecognitionSubscription faceRecognitionSubscription = new FaceRecognitionSubscription(camera, attendanceCacheQueue);
      Thread faceRecognitionThread = new Thread(faceRecognitionSubscription);
      cameraIdFaceSubscriptionMap.putIfAbsent(camera.getId(), faceRecognitionThread);
      faceRecognitionThread.start();
      cameraRepository.setCameraStatusOfCamera(camera.getId(), CameraStatus.PERFORMING_FACE_RECOGNITION);
    }
  }

  @Override
  public void stopFaceRecognitionSubscription(Long organizationId) {
    Map<Long, Thread> cameraIdFaceSubscriptionMap = organizationIdFaceRecognitionSubscriptionsMap.get(organizationId);
    for (Map.Entry<Long, Thread> entry : cameraIdFaceSubscriptionMap.entrySet()) {
      entry.getValue().interrupt();
      cameraRepository.setCameraStatusOfCamera(entry.getKey(), CameraStatus.INACTIVE);
    }
  }
}
