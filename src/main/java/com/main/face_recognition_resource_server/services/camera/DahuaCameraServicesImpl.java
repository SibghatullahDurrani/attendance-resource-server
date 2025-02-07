package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.main.face_recognition_resource_server.components.AttendanceCache;
import com.main.face_recognition_resource_server.components.AttendanceCacheImpl;
import com.main.face_recognition_resource_server.components.BlockingQueueAttendanceCacheConsumer;
import com.main.face_recognition_resource_server.converters.CameraToCameraDTOConvertor;
import com.main.face_recognition_resource_server.domains.Camera;
import com.main.face_recognition_resource_server.helpers.SubscriptionLockInstance;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.services.camera.dahua.FaceRecognitionSubscription;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class DahuaCameraServicesImpl implements CameraServices {
  private final CameraRepository cameraRepository;
  private final CameraToCameraDTOConvertor cameraToCameraDTOConvertor;
  private Map<String, SubscriptionLockInstance> subscriptions = new HashMap<>();


  public DahuaCameraServicesImpl(CameraRepository cameraRepository, CameraToCameraDTOConvertor cameraToCameraDTOConvertor) {
    this.cameraRepository = cameraRepository;
    this.cameraToCameraDTOConvertor = cameraToCameraDTOConvertor;
  }

  @Override
  public void startInFaceRecognitionSubscription(String ipAddress, int port) {
    Optional<Camera> optionalCamera = cameraRepository.findCameraByIpAddressAndPort(ipAddress, port);
    optionalCamera.ifPresentOrElse(camera -> {
      BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue = new LinkedBlockingQueue<>();
      Object synchronizationLock = new Object();
      AttendanceCache residentCache = new AttendanceCacheImpl(synchronizationLock);
      BlockingQueueAttendanceCacheConsumer attendanceCacheConsumer = new BlockingQueueAttendanceCacheConsumer(attendanceCacheQueue, residentCache, null, synchronizationLock);
      CameraDTO cameraDTO = cameraToCameraDTOConvertor.convert(camera);
      FaceRecognitionSubscription subscription = new FaceRecognitionSubscription(cameraDTO, attendanceCacheQueue);
      Thread faceSubscriptionThread = new Thread(subscription);
      Thread attendanceCacheConsumerThread = new Thread(attendanceCacheConsumer);
      faceSubscriptionThread.start();
      attendanceCacheConsumerThread.start();
      subscriptions.put(ipAddress, new SubscriptionLockInstance(synchronizationLock, subscription));
    }, () -> {
      throw new RuntimeException();
    });
  }
}
