package com.main.face_recognition_resource_server.components.attendancecacheproducer;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.constants.CameraType;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueAttendanceCacheProducer implements AttendanceCacheProducer {
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;

  public BlockingQueueAttendanceCacheProducer(BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    this.attendanceCacheQueue = attendanceCacheQueue;
  }

  @Override
  public void produceCache(Long userId, Date time, CameraType cameraType, BufferedImage image) throws InterruptedException {
    attendanceCacheQueue.put(AttendanceCacheDTO.builder()
            .userId(userId)
            .time(time)
            .cameraType(cameraType)
            .image(image)
            .build());
  }
}
