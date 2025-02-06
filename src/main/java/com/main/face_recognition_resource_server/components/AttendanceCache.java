package com.main.face_recognition_resource_server.components;

public interface AttendanceCache {
  boolean isUserInCache(Long userId);

  void addUserToCache(Long userId);

  void invalidateCache();

  void removeUserFromCache(Long userId);
}
