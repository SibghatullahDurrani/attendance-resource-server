package com.main.face_recognition_resource_server.components.attendancecacheconsumer;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.components.attendancecache.AttendanceCache;
import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLock;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
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
  private final String retakeAttendanceCron;
  private final TaskScheduler taskScheduler;

  public BlockingQueueAttendanceCacheConsumer(BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue, AttendanceCache residentCache, AttendanceCache nonResidentCache, SynchronizationLock synchronizationLock, AttendanceServices attendanceServices, String retakeAttendanceCron, TaskScheduler taskScheduler) {
    this.attendanceCacheQueue = attendanceCacheQueue;
    this.residentCache = residentCache;
    this.nonResidentCache = nonResidentCache;
    this.synchronizationLock = synchronizationLock;
    this.attendanceServices = attendanceServices;
    this.retakeAttendanceCron = retakeAttendanceCron;
    this.taskScheduler = taskScheduler;
  }

  @PostConstruct
  public void scheduleCacheInvalidation() {
    if (retakeAttendanceCron.equals("0 0 0 * * *")) {
      taskScheduler.schedule(this::invalidateCache, new CronTrigger(retakeAttendanceCron));
    } else {
      taskScheduler.schedule(this::invalidateCache, new CronTrigger(retakeAttendanceCron));
      taskScheduler.schedule(this::invalidateCache, new CronTrigger("0 0 0 * * *"));
    }
  }


  @Override
  public void run() {
    while (true) {
      try {
        AttendanceCacheDTO attendanceCache = attendanceCacheQueue.take();
        if (nonResidentCache == null) {
          handleDataWithJustResidentCache(attendanceCache.getUserId(), attendanceCache.getTime(), attendanceCache.getFullImage(), attendanceCache.getFaceImage());
        } else {
          handleDataWithBothResidentAndNonResidentCache(attendanceCache.getUserId(), attendanceCache.getTime(), attendanceCache.getCameraType(), attendanceCache.getFullImage(), attendanceCache.getFaceImage());
        }
      } catch (InterruptedException | UserDoesntExistException | IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void handleDataWithBothResidentAndNonResidentCache(Long userId, Date time, CameraType cameraType, BufferedImage fullImage, BufferedImage faceImage) throws UserDoesntExistException, IOException {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(userId)) {
        if (!nonResidentCache.isUserInCache(userId)) {
          if (cameraType == CameraType.IN) {
            attendanceServices.markCheckIn(userId, time, fullImage, faceImage);
            residentCache.addUserToCache(userId);
            System.out.println("user: " + userId.toString() + " check in");
          }
        } else {
          if (cameraType == CameraType.IN) {
            nonResidentCache.removeUserFromCache(userId);
            residentCache.addUserToCache(userId);
            attendanceServices.markCheckIn(userId, time, fullImage, faceImage);
            System.out.println("user: " + userId.toString() + " check in");
          }
        }
      } else {
        if (!nonResidentCache.isUserInCache(userId)) {
          if (cameraType == CameraType.OUT) {
            residentCache.removeUserFromCache(userId);
            nonResidentCache.addUserToCache(userId);
            attendanceServices.markCheckOut(userId, time, fullImage, faceImage);
            System.out.println("user: " + userId.toString() + " check out");
          }
        }
      }
    }
  }

  private void handleDataWithJustResidentCache(Long userId, Date time, BufferedImage fullImage, BufferedImage faceImage) throws UserDoesntExistException, IOException {
    synchronized (synchronizationLock) {
      if (!residentCache.isUserInCache(userId)) {
        attendanceServices.markCheckIn(userId, time, fullImage, faceImage);
        residentCache.addUserToCache(userId);
        System.out.println("user with id: " + userId + " added to cache at time: " + time);
      } else {
        System.out.println("user with id: " + userId + " is already in the cache");
      }
    }
  }

  public void invalidateCache() {
    if (residentCache != null) {
      residentCache.invalidateCache();
    }
    if (nonResidentCache != null) {
      nonResidentCache.invalidateCache();
    }
  }
}
