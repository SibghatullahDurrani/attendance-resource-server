package com.main.face_recognition_resource_server.components;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.constants.CameraTypes;

import java.util.Date;
import java.util.concurrent.BlockingQueue;


public class BlockingQueueAttendanceCacheConsumer implements Runnable {
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;
  private final AttendanceCache residentCache;
  private final AttendanceCache nonResidentCache;
  private final Object synchronizationLock;

  public BlockingQueueAttendanceCacheConsumer(BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue, AttendanceCache residentCache, AttendanceCache nonResidentCache, Object synchronizationLock) {
    this.attendanceCacheQueue = attendanceCacheQueue;
    this.residentCache = residentCache;
    this.nonResidentCache = nonResidentCache;
    this.synchronizationLock = synchronizationLock;
  }

  @Override
  public void run() {
    while (true) {
      try {
        AttendanceCacheDTO attendanceCache = attendanceCacheQueue.take();
        if (nonResidentCache == null) {
          handleDataWithJustResidentCache(attendanceCache.getUserId(), attendanceCache.getTime());
        } else {
          handleDataWithBothResidentAndNonResidentCache(attendanceCache.getUserId(), attendanceCache.getTime(), attendanceCache.getCameraType());
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

    }
  }

  private void handleDataWithBothResidentAndNonResidentCache(Long userId, Date time, CameraTypes cameraType) {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(userId)) {
        if (!nonResidentCache.isUserInCache(userId)) {
          if (cameraType == CameraTypes.IN) {
            //TODO: add an entry for attendance worker
            residentCache.addUserToCache(userId);
          }
        } else {
          //TODO: add an entry for attendance worker
          nonResidentCache.removeUserFromCache(userId);
          residentCache.addUserToCache(userId);
        }
      }
    }
  }

  private void handleDataWithJustResidentCache(Long id, Date time) {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(id)) {
        //TODO: add an entry for attendance worker
        residentCache.addUserToCache(id);
        System.out.println("user with id: " + id + " added to cache at time: " + time);
      } else {
        System.out.println("user with id: " + id + " is already in the cache");
      }
    }
  }
}
