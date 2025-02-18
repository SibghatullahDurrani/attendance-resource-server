package com.main.face_recognition_resource_server.components.attendancecache;

import com.main.face_recognition_resource_server.constants.CameraType;

public interface AttendanceCache {
  boolean isUserInCache(Long userId);

  void addUserToCache(Long userId);

  void invalidateCache();

  void removeUserFromCache(Long userId);

  void syncCache(Long organizationId, CameraType type);
}
