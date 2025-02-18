package com.main.face_recognition_resource_server.components;

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
    if (!applicationContext.containsBean(beanName)) {
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
      BeanDefinition beanDefinition = BeanDefinitionBuilder
              .genericBeanDefinition(AttendanceCache.class,
                      () -> applicationContext.getBean(AttendanceCache.class, synchronizationLockFactory.getSynchronizationLock(organizationId)))
              .getBeanDefinition();
      beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }
    return (AttendanceCache) applicationContext.getBean(beanName);
  }
}
