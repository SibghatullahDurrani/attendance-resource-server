package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.main.face_recognition_resource_server.converters.CameraToCameraDTOConvertor;
import com.main.face_recognition_resource_server.domains.Camera;
import com.main.face_recognition_resource_server.helpers.SubscriptionLockInstance;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.services.AttendanceCache;
import com.main.face_recognition_resource_server.services.AttendanceCacheImpl;
import com.main.face_recognition_resource_server.services.camera.dahua.FaceRecognitionSubscription;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
      CameraDTO cameraDTO = cameraToCameraDTOConvertor.convert(camera);
      Object synchronizationLock = new Object();
      AttendanceCache residentCache = new AttendanceCacheImpl(synchronizationLock);
      FaceRecognitionSubscription subscription = new FaceRecognitionSubscription(residentCache, null, cameraDTO, synchronizationLock);
      Thread thread = new Thread(subscription);
      thread.start();
      subscriptions.put(ipAddress, new SubscriptionLockInstance(synchronizationLock, subscription));
    }, () -> {
      throw new RuntimeException();
    });


    //TODO: ADD THE REMAINING FUNCTIONALITY TO START LISTENING TO FACE RECOGNITION DATA
    //TODO: LEARN CONCURRENCY

  }
}
