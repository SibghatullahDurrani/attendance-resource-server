package com.main.face_recognition_resource_server.components.attendancecachequeuefactory;


import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AttendanceCacheQueueFactory {
  private final Map<Long, BlockingQueue<AttendanceCacheDTO>> organizationIdCacheQueueMap;

  public AttendanceCacheQueueFactory() {
    organizationIdCacheQueueMap = new HashMap<>();
  }

  public BlockingQueue<AttendanceCacheDTO> getAttendanceCacheQueue(Long organizationId) {
    if (organizationIdCacheQueueMap.containsKey(organizationId)) {
      return organizationIdCacheQueueMap.get(organizationId);
    } else {
      BlockingQueue<AttendanceCacheDTO> queue = new LinkedBlockingQueue<>();
      organizationIdCacheQueueMap.put(organizationId, queue);
      return queue;
    }
  }
}
