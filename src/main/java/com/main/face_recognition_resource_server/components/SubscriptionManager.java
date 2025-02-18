package com.main.face_recognition_resource_server.components;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.components.attendancecacheconsumer.BlockingQueueAttendanceCacheConsumer;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Data
public class SubscriptionManager {
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;
  private final Object synchronizationLock;
  private final Map<Long, Thread> faceRecognitionSubscriptions;
  private BlockingQueueAttendanceCacheConsumer attendanceCacheConsumer;

  public SubscriptionManager(BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue, Object synchronizationLock, BlockingQueueAttendanceCacheConsumer attendanceCacheConsumer) {
    this.attendanceCacheQueue = attendanceCacheQueue;
    this.synchronizationLock = synchronizationLock;
    this.attendanceCacheConsumer = attendanceCacheConsumer;
    this.faceRecognitionSubscriptions = new HashMap<>();
  }

  public void addFaceRecognitionSubscription(Long cameraId, Thread faceRecognitionSubscription) {
    faceRecognitionSubscriptions.putIfAbsent(cameraId, faceRecognitionSubscription);
  }
}
