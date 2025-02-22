package com.main.face_recognition_resource_server.components.attendancecacheconsumer;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.components.attendancecache.AttendanceCache;
import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLock;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;


@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
public class BlockingQueueAttendanceCacheConsumer implements Runnable {
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;
  private final AttendanceCache residentCache;
  private final AttendanceCache nonResidentCache;
  private final SynchronizationLock synchronizationLock;
  private final AttendanceServices attendanceServices;
  private boolean isFirstCacheInvalidation = true;

  public BlockingQueueAttendanceCacheConsumer(BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue, AttendanceCache residentCache, AttendanceCache nonResidentCache, SynchronizationLock synchronizationLock, AttendanceServices attendanceServices) {
    this.attendanceCacheQueue = attendanceCacheQueue;
    this.residentCache = residentCache;
    this.nonResidentCache = nonResidentCache;
    this.synchronizationLock = synchronizationLock;
    this.attendanceServices = attendanceServices;
//    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//    scheduledExecutorService.scheduleAtFixedRate(() -> {
//      if (!isFirstCacheInvalidation) {
//        if (residentCache != null) {
//          residentCache.invalidateCache();
//        }
//        if (nonResidentCache != null) {
//          nonResidentCache.invalidateCache();
//        }
//        System.out.println("Cache invalidated");
//      }
//      isFirstCacheInvalidation = false;
//    }, 1, 3600, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    while (true) {
      try {
        AttendanceCacheDTO attendanceCache = attendanceCacheQueue.take();
        if (nonResidentCache == null) {
          handleDataWithJustResidentCache(attendanceCache.getUserId(), attendanceCache.getTime(), attendanceCache.getImage());
        } else {
          handleDataWithBothResidentAndNonResidentCache(attendanceCache.getUserId(), attendanceCache.getTime(), attendanceCache.getCameraType(), attendanceCache.getImage());
        }
      } catch (InterruptedException | UserDoesntExistException | IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void handleDataWithBothResidentAndNonResidentCache(Long userId, Date time, CameraType cameraType, BufferedImage image) throws UserDoesntExistException, IOException {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(userId)) {
        if (!nonResidentCache.isUserInCache(userId)) {
          if (cameraType == CameraType.IN) {
            attendanceServices.markCheckIn(userId, time, image);
            residentCache.addUserToCache(userId);
            System.out.println("user: " + userId.toString() + " check in");
          }
        } else {
          if (cameraType == CameraType.IN) {
            nonResidentCache.removeUserFromCache(userId);
            residentCache.addUserToCache(userId);
            attendanceServices.markCheckIn(userId, time, image);
            System.out.println("user: " + userId.toString() + " check in");
          }
        }
      } else {
        if (!nonResidentCache.isUserInCache(userId)) {
          if (cameraType == CameraType.OUT) {
            residentCache.removeUserFromCache(userId);
            nonResidentCache.addUserToCache(userId);
            attendanceServices.markCheckOut(userId, time, image);
            System.out.println("user: " + userId.toString() + " check out");
          }
        }
      }
    }
  }

  private void handleDataWithJustResidentCache(Long userId, Date time, BufferedImage image) throws UserDoesntExistException, IOException {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(userId)) {
        attendanceServices.markCheckIn(userId, time, image);
        residentCache.addUserToCache(userId);
        System.out.println("user with id: " + userId + " added to cache at time: " + time);
      } else {
        System.out.println("user with id: " + userId + " is already in the cache");
      }
    }
  }

  @Scheduled(cron = "0 0 * * * *")
  public void invalidateCacheEveryHour() {
    if (residentCache != null) {
      residentCache.invalidateCache();
    }
    if (nonResidentCache != null) {
      nonResidentCache.invalidateCache();
    }
    System.out.println("Cache invalidated");
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void invalidateCacheAtTheEndOfTheDay() {
    if (residentCache != null) {
      residentCache.invalidateCache();
    }
    if (nonResidentCache != null) {
      nonResidentCache.invalidateCache();
    }
    System.out.println("Cache invalidated");
  }
}
