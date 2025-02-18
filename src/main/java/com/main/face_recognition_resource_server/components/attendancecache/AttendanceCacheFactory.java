package com.main.face_recognition_resource_server.components.attendancecache;

import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLock;
import com.main.face_recognition_resource_server.components.synchronizationlock.SynchronizationLockFactory;
import com.main.face_recognition_resource_server.constants.BeanNamePrefix;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AttendanceCacheFactory {
  private final ApplicationContext applicationContext;
  private final SynchronizationLockFactory synchronizationLockFactory;

  public AttendanceCacheFactory(ApplicationContext applicationContext, SynchronizationLockFactory synchronizationLockFactory) {
    this.applicationContext = applicationContext;
    this.synchronizationLockFactory = synchronizationLockFactory;
  }

  public AttendanceCache getResidentCache(Long organizationId) {
    String beanName = BeanNamePrefix.RESIDENT_CACHE + organizationId.toString();
    return getAttendanceCache(organizationId, beanName);
  }

  public AttendanceCache getNonResidentCache(Long organizationId) {
    String beanName = BeanNamePrefix.NON_RESIDENT_CACHE + organizationId.toString();
    return getAttendanceCache(organizationId, beanName);
  }

  private AttendanceCache getAttendanceCache(Long organizationId, String beanName) {
    if (!applicationContext.containsBean(beanName)) {
      SynchronizationLock synchronizationLock = synchronizationLockFactory.getSynchronizationLock(organizationId);
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
      BeanDefinition beanDefinition = BeanDefinitionBuilder
              .genericBeanDefinition(AttendanceCache.class,
                      () -> new AttendanceCacheImpl(synchronizationLock))
              .setScope(BeanDefinition.SCOPE_PROTOTYPE)
              .getBeanDefinition();
      beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }
    return (AttendanceCache) applicationContext.getBean(beanName);
  }
}
