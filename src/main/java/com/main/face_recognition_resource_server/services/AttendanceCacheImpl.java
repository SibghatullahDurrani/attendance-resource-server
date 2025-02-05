package com.main.face_recognition_resource_server.services;

import java.util.Set;
import java.util.TreeSet;

public class AttendanceCacheImpl implements AttendanceCache {
  private final Object synchronizationLock;
  private Set<Long> usersRecognizedCache;

  public AttendanceCacheImpl(Object synchronizationLock) {
    this.synchronizationLock = synchronizationLock;
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
      usersRecognizedCache = new TreeSet<>();
    }
  }

  @Override
  public void removeUserFromCache(Long userId) {
    synchronized (synchronizationLock) {
      usersRecognizedCache.remove(userId);
    }
  }
}
