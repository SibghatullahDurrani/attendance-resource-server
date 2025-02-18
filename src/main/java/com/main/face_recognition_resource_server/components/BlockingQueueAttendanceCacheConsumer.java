package com.main.face_recognition_resource_server.components;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.constants.CameraType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
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
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    scheduledExecutorService.scheduleAtFixedRate(() -> {
      if (residentCache != null) {
        residentCache.invalidateCache();
      }
      if (nonResidentCache != null) {
        nonResidentCache.invalidateCache();
      }
      System.out.println("Cache invalidated");
    }, 0, 1, TimeUnit.HOURS);
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

  private void handleDataWithBothResidentAndNonResidentCache(Long userId, Date time, CameraType cameraType) {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(userId)) {
        if (!nonResidentCache.isUserInCache(userId)) {
          if (cameraType == CameraType.IN) {
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
