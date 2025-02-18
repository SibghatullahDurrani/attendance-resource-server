package com.main.face_recognition_resource_server.components.synchronizationlock;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SynchronizationLockFactory {
  private final Map<Long, SynchronizationLock> organizationIdSynchronizationLockMap;

  public SynchronizationLockFactory() {
    organizationIdSynchronizationLockMap = new HashMap<>();
  }

  public SynchronizationLock getSynchronizationLock(Long organizationId) {
    if (organizationIdSynchronizationLockMap.containsKey(organizationId)) {
      return organizationIdSynchronizationLockMap.get(organizationId);
    } else {
      SynchronizationLock synchronizationLock = new SynchronizationLock();
      organizationIdSynchronizationLockMap.put(organizationId, synchronizationLock);
      return synchronizationLock;
    }
  }
}
