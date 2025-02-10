package com.main.face_recognition_resource_server.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = "spring.flyway.clean-disabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractPostgreSQLTestContainer.Initializer.class)
@Testcontainers
public abstract class AbstractPostgreSQLTestContainer {
  public static PostgreSQLContainer<?> database;

  static {
    database = new PostgreSQLContainer<>("postgres:17");
    database.start();
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
              applicationContext,
              "spring.datasource.url=" + database.getJdbcUrl(),
              "spring.datasource.username=" + database.getUsername(),
              "spring.datasource.password=" + database.getPassword()
      );
    }
  }
}
