# Agent Guidelines for attendance-resource-server

## Build & Test Commands
```bash
mvn clean compile              # Clean and compile
mvn test                       # Run all tests  
mvn test -Dtest=ClassName     # Run single test class
mvn test -Dtest=ClassName#methodName  # Run single test method
mvn spring-boot:run           # Run application
./mvnw spring-boot:run        # Alternative with wrapper
```

## Code Style
- **Package**: `com.main.face_recognition_resource_server`
- **Java Version**: 23, Spring Boot 3.4.2
- **Imports**: Group by package (java.*, javax.*, org.springframework.*, project imports)
- **Naming**: CamelCase classes, camelCase methods/variables, UPPER_SNAKE constants
- **DTOs**: Place in DTOS package with descriptive suffixes (DTO, RequestDTO, MessageDTO)
- **Annotations**: Use Lombok (@Slf4j, @Data), Spring (@RestController, @Service, @Repository)
- **REST**: Follow RESTful patterns, use ResponseEntity with proper HTTP status codes
- **Security**: @PreAuthorize for authorization, never commit secrets
- **Testing**: Use @SpringBootTest with @ActiveProfiles("test"), mock external services
- **Error Handling**: Custom exceptions in exceptions package, handle with @ControllerAdvice
- **Database**: PostgreSQL with Flyway migrations, JPA repositories
- **Message Queue**: RabbitMQ for async communication