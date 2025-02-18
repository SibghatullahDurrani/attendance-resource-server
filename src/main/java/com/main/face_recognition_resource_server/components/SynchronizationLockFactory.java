package com.main.face_recognition_resource_server.components;

import com.main.face_recognition_resource_server.constants.BeanNamePrefix;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SynchronizationLockFactory {
  private final ApplicationContext applicationContext;

  public SynchronizationLockFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public Object getSynchronizationLock(Long organizationId) {
    String beanName = BeanNamePrefix.SYNCHRONIZATION_LOCK + organizationId.toString();
    if (!applicationContext.containsBean(beanName)) {
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
      BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(Object.class).getBeanDefinition();
      beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }
    return applicationContext.getBean(beanName);
  }
}
