# Smart Bill Payment Services

A professional Spring Boot 3 application for MBME Bill Payment and Utilities Payment System built with modern Java 17+ features.

## Features

- **Spring Boot 3.4.3** - Latest stable version with enhanced performance and features
- **Java 17** - Modern Java with records, pattern matching, and enhanced APIs
- **Professional Architecture** - Clean separation of concerns with proper layering
- **API Documentation** - Integrated Swagger/OpenAPI 3 with SpringDoc
- **Multi-Profile Support** - Separate configurations for dev, UAT, and production
- **Comprehensive Logging** - Structured logging with Logback and custom masking
- **Database Integration** - JPA/Hibernate with H2 for development
- **REST Client** - Apache HttpClient 5 for external API integrations
- **Testing** - Comprehensive test suite with Mockito

## Technology Stack

- **Framework**: Spring Boot 3.4.3
- **Java Version**: 17 (LTS)
- **Build Tool**: Maven 3.8+
- **Database**: H2 (development), configurable for production
- **Documentation**: SpringDoc OpenAPI 3 (v2.7.0)
- **HTTP Client**: Apache HttpComponents 5
- **Logging**: Logback with custom converters
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smart-bill-payment-services
   ```

2. **Build the application**
   ```bash
   ./mvnw clean compile
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - Application: http://localhost:8081
   - Swagger UI: http://localhost:8081/swagger-ui.html
   - H2 Console: http://localhost:8081/h2-console

## Configuration Profiles

The application supports multiple deployment environments:

### Development Profile (`dev`)
- Debug logging enabled
- H2 database with DDL auto-creation
- Detailed error responses
- Console appender for logs

### UAT Profile (`uat`) - Default
- Standard logging configuration
- Database validation mode
- File-based logging
- Production-like settings for testing

### Production Profile (`prod`)
- Minimal logging (WARN level)
- Secure error handling
- File-based logging with rotation
- Performance optimizations

### Switching Profiles

Set the active profile in `application.properties`:
```properties
spring.profiles.active=dev  # or uat, prod
```

Or use command line:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Endpoints

### Token Management
- `GET /mbme/api/token` - Retrieve valid authentication token

### Bill Services
- `GET /mbme-bill-service/api/hello` - Health check endpoint
- `POST /mbme-bill-service/api/bill-inquiry` - Process bill inquiry
- `POST /mbme-bill-service/api/bill-payment` - Process bill payment

## Architecture

### Package Structure
```
com.sesami.smart_bill_payment_services/
├── config/           # Configuration classes
├── common/           # Shared utilities and exceptions
├── exception/        # Custom exception handling
└── mbme/
    ├── token/        # Token management module
    │   ├── controller/
    │   ├── service/
    │   ├── entity/
    │   └── repository/
    └── billpayment/  # Bill payment module
        ├── controller/
        ├── service/
        ├── bean/
        ├── entity/
        └── repository/
```

### Key Components

- **Controllers**: RESTful endpoints with proper HTTP status handling
- **Services**: Business logic implementation
- **Repositories**: Data access layer with Spring Data JPA
- **Entities**: JPA entities for database mapping
- **Configuration**: Custom beans and application setup

## Logging

The application uses Logback with profile-specific configurations:

- **Custom Masking**: Sensitive data is automatically masked in logs
- **Rolling Files**: Daily log rotation with 30-day retention
- **Structured Format**: Consistent timestamp and thread information

## Security Features

- **Data Masking**: Sensitive information in logs is automatically masked
- **Error Handling**: Production-safe error responses
- **Input Validation**: Request validation with proper error messages

## Integration

### MBME API Integration
The application integrates with MBME services for:
- OAuth token management
- Bill inquiry operations
- Payment processing
- Transaction reporting
- Balance inquiries

Configuration properties are externalized and support multiple environments.

## Development

### Code Style
- Uses modern Java features (records, enhanced switch, etc.)
- Follows Spring Boot best practices
- Implements proper error handling and logging
- Uses constructor-based dependency injection

### Testing
Run tests with:
```bash
./mvnw test
```

### Building for Production
```bash
./mvnw clean package -Pprod
```

## Monitoring and Observability

- **Health Checks**: Spring Boot Actuator endpoints
- **Metrics**: Application performance metrics
- **Logging**: Comprehensive logging with correlation IDs
- **API Documentation**: Real-time API documentation via Swagger

## Deployment

The application is packaged as a WAR file for deployment to application servers or can be run as a standalone JAR.

### WAR Deployment
- Compatible with Tomcat 10+
- Servlet 5.0+ specification
- Java 17+ runtime required

### Environment Variables
Key configuration can be overridden via environment variables:
- `SPRING_PROFILES_ACTIVE`: Set active profile
- `SERVER_PORT`: Override default port (8081)
- `LOGGING_FILE_PATH`: Custom log file location

## Contributing

1. Follow the existing code style and patterns
2. Ensure all tests pass
3. Update documentation for new features
4. Use meaningful commit messages

## License

This project is proprietary software for Sesami Middle East.
