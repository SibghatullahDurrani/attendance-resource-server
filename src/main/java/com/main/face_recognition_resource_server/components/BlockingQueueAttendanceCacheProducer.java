package com.main.face_recognition_resource_server.components;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.enums.CameraTypes;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueAttendanceCacheProducer implements AttendanceCacheProducer {
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;

  public BlockingQueueAttendanceCacheProducer(BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    this.attendanceCacheQueue = attendanceCacheQueue;
  }

  @Override
  public void produceCache(Long userId, Date time, CameraTypes cameraType) throws InterruptedException {
    attendanceCacheQueue.put(AttendanceCacheDTO.builder()
            .userId(userId)
            .time(time)
            .cameraType(cameraType)
            .build());
  }
}
