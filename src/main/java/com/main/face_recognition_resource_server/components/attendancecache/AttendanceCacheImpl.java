package com.main.face_recognition_resource_server.components.attendancecache;

import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLock;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.services.attendance.AttendanceServices;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
public class AttendanceCacheImpl implements AttendanceCache {
  private final SynchronizationLock synchronizationLock;
  private final AttendanceServices attendanceServices;
  private final Set<Long> usersRecognizedCache;

  public AttendanceCacheImpl(SynchronizationLock synchronizationLock, AttendanceServices attendanceServices) {
    this.synchronizationLock = synchronizationLock;
    this.attendanceServices = attendanceServices;
    usersRecognizedCache = new TreeSet<>();
  }

  @Override
  public boolean isUserInCache(Long userId) {
    synchronized (synchronizationLock) {
      return usersRecognizedCache.contains(userId);
    }
  }

  @Override
  public void addUserToCache(Long userId) {
    synchronized (synchronizationLock) {
      usersRecognizedCache.add(userId);
    }
  }

  @Override
  public void invalidateCache() {
    synchronized (synchronizationLock) {
      usersRecognizedCache.clear();
    }
  }

  @Override
  public void removeUserFromCache(Long userId) {
    synchronized (synchronizationLock) {
      usersRecognizedCache.remove(userId);
    }
  }

  @Override
  public void syncCache(Long organizationId, CameraType type) {
    Set<Long> cache = attendanceServices.getCache(organizationId, type);
    usersRecognizedCache.addAll(cache);
  }
}
