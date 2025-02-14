package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterCameraDTO;
import com.main.face_recognition_resource_server.components.AttendanceCache;
import com.main.face_recognition_resource_server.components.AttendanceCacheImpl;
import com.main.face_recognition_resource_server.components.BlockingQueueAttendanceCacheConsumer;
import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.converters.CameraToCameraDTOConvertor;
import com.main.face_recognition_resource_server.domains.Camera;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInDepartmentException;
import com.main.face_recognition_resource_server.exceptions.CameraCanOnlyBelongToOneTypeException;
import com.main.face_recognition_resource_server.helpers.SubscriptionLockInstance;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.services.camera.dahua.FaceRecognitionSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class CameraServicesImpl implements CameraServices {
  private final CameraRepository cameraRepository;
  private Map<String, SubscriptionLockInstance> subscriptions = new HashMap<>();


  public CameraServicesImpl(CameraRepository cameraRepository) {
    this.cameraRepository = cameraRepository;
  }

  @Override
  public void startInFaceRecognitionSubscription(String ipAddress, int port) {
    Optional<Camera> optionalCamera = cameraRepository.getCameraByIpAddressAndPort(ipAddress, port);
    optionalCamera.ifPresentOrElse(camera -> {
      BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue = new LinkedBlockingQueue<>();
      Object synchronizationLock = new Object();
      AttendanceCache residentCache = new AttendanceCacheImpl(synchronizationLock);
      BlockingQueueAttendanceCacheConsumer attendanceCacheConsumer = new BlockingQueueAttendanceCacheConsumer(attendanceCacheQueue, residentCache, null, synchronizationLock);
      CameraDTO cameraDTO = CameraToCameraDTOConvertor.convert(camera);
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

  @Override
  public List<DepartmentCameraDTO> getCamerasOfDepartment(Long departmentId) {
    return cameraRepository.getCamerasOfDepartment(departmentId);
  }

  @Override
  public boolean cameraExistInDepartment(Long departmentId, List<Department> departments) throws CameraAlreadyExistsInDepartmentException {
    boolean exists = departments.stream().anyMatch(department -> department.getId().equals(departmentId));
    if (exists) {
      throw new CameraAlreadyExistsInDepartmentException();
    } else {
      return false;
    }
  }

  @Override
  @Transactional
  public void registerCamera(RegisterCameraDTO cameraToRegister, Department department) {
    Optional<Camera> optionalCamera = cameraRepository.getCameraByIpAddressPortAndChannel(
            cameraToRegister.getIpAddress(),
            cameraToRegister.getPort(),
            cameraToRegister.getChannel()
    );
    if (optionalCamera.isPresent()) {
      if (optionalCamera.get().getType() != cameraToRegister.getType()) {
        throw new CameraCanOnlyBelongToOneTypeException();
      }
      if (!cameraExistInDepartment(cameraToRegister.getDepartmentId(), optionalCamera.get().getDepartments())) {
        optionalCamera.get().getDepartments().add(department);
        cameraRepository.saveAndFlush(optionalCamera.get());
      }
    } else {
      List<Department> departments = List.of(department);
      Camera camera = Camera.builder()
              .ipAddress(cameraToRegister.getIpAddress())
              .port(cameraToRegister.getPort())
              .channel(cameraToRegister.getChannel())
              .username(cameraToRegister.getUsername())
              .password(cameraToRegister.getPassword())
              .type(cameraToRegister.getType())
              .cameraStatus(CameraStatus.INACTIVE)
              .departments(departments)
              .build();
      cameraRepository.saveAndFlush(camera);
    }
  }
}
