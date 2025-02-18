package com.main.face_recognition_resource_server.components.attendancecacheconsumer;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.components.attendancecache.AttendanceCache;
import com.main.face_recognition_resource_server.components.attendancecache.AttendanceCacheFactory;
import com.main.face_recognition_resource_server.components.attendancecachequeuefactory.AttendanceCacheQueueFactory;
import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLock;
import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLockFactory;
import com.main.face_recognition_resource_server.constants.BeanNamePrefix;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Component
public class AttendanceCacheConsumerFactory {
  private final ApplicationContext applicationContext;
  private final AttendanceCacheQueueFactory attendanceCacheQueueFactory;
  private final AttendanceCacheFactory attendanceCacheFactory;
  private final SynchronizationLockFactory synchronizationLockFactory;

  public AttendanceCacheConsumerFactory(ApplicationContext applicationContext, AttendanceCacheQueueFactory attendanceCacheQueueFactory, AttendanceCacheFactory attendanceCacheFactory, SynchronizationLockFactory synchronizationLockFactory) {
    this.applicationContext = applicationContext;
    this.attendanceCacheQueueFactory = attendanceCacheQueueFactory;
    this.attendanceCacheFactory = attendanceCacheFactory;
    this.synchronizationLockFactory = synchronizationLockFactory;
  }

  public BlockingQueueAttendanceCacheConsumer getAttendanceCacheConsumer(Long organizationId) {
    String beanName = BeanNamePrefix.ATTENDANCE_CACHE_CONSUMER + organizationId.toString();
    if (!applicationContext.containsBean(beanName)) {
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
      BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue = attendanceCacheQueueFactory.getAttendanceCacheQueue(organizationId);
      AttendanceCache residentCache = attendanceCacheFactory.getResidentCache(organizationId);
      AttendanceCache nonResidentCache;
      SynchronizationLock synchronizationLock = synchronizationLockFactory.getSynchronizationLock(organizationId);
      if (applicationContext.containsBean(BeanNamePrefix.NON_RESIDENT_CACHE + organizationId.toString())) {
        nonResidentCache = attendanceCacheFactory.getNonResidentCache(organizationId);
      } else {
        nonResidentCache = null;
      }
      BeanDefinition beanDefinition = BeanDefinitionBuilder
              .genericBeanDefinition(BlockingQueueAttendanceCacheConsumer.class,
                      () -> new BlockingQueueAttendanceCacheConsumer(
                              attendanceCacheQueue,
                              residentCache,
                              nonResidentCache,
                              synchronizationLock
                      )
              )
              .setScope(BeanDefinition.SCOPE_PROTOTYPE)
              .getBeanDefinition();
      beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }
    return (BlockingQueueAttendanceCacheConsumer) applicationContext.getBean(beanName);
  }
}
