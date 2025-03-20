# ProjectName

## Overview

ProjectName is a Spring Boot 3 application that provides services for token management and DuBillPayment processing. The application uses JPA for database interactions and RestTemplate for making HTTP requests to external APIs. All activities related to token and bill payment services are logged in the database.

## Features

- Fetch and save token details
- Process DuBillPayment requests
- Validate token expiration and fetch new tokens if expired
- Log all token and bill payment activities in the database

## Technologies Used

- Spring Boot 3
- Spring Data JPA
- RestTemplate
- H2 Database (or any other database of your choice)
- JUnit and Mockito for testing
- JDK 21

## Package Structure
com.example.projectname ├── config │ └── AppConfig.java ├── controller │ └── TokenController.java │ └── DuBillPaymentController.java ├── dto │ └── DuBillPaymentRequest.java │ └── DuBillPaymentResponse.java ├── entity │ └── TokenDetails.java │ └── DuBillPayment.java │ └── TokenActivity.java │ └── BillPaymentActivity.java ├── repository │ └── TokenDetailsRepository.java │ └── DuBillPaymentRepository.java │ └── TokenActivityRepository.java │ └── BillPaymentActivityRepository.java ├── service │ └── TokenService.java │ └── DuBillPaymentService.java ├── exception │ └── CustomException.java ├── util │ └── UtilityClass.java ├── ProjectNameApplication.java └── resources └── application.properties


## Getting Started

### Prerequisites

- JDK 21
- Maven
- An IDE (e.g., IntelliJ IDEA, Eclipse)

### Installation

1. Clone the repository:

```sh
git clone https://github.com/yourusername/projectname.git
```

Navigate to the project directory:
cd projectname
Build the project using Maven:
mvn clean install
Run the application:
mvn spring-boot:run
Configuration
Update the application.properties file with your database configuration:

spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
Endpoints
Fetch Token: /api/fetch-token

Method: POST
Parameters: username, password
Description: Fetches a valid token, either from the database or by calling the external API if the token is expired.
Process DuBillPayment: /api/du-bill-payment

Method: POST
Request Body: DuBillPaymentRequest
Description: Processes DuBillPayment requests and saves the details in the database.


Run the tests using Maven:

mvn test

Building Multiple .war Files
To generate multiple .war files for different configurations or environments, use Maven profiles:

Add profiles to your pom.xml:
<profiles>
    <profile>
       profile1</id>
        <build>
            <finalName>projectname-profile1</finalName>
        </build>
    </profile>
    <profile>
        <id>profile2</id>
        <build>
            <finalName>projectname-profile2</finalName>
        </build>
    </profile>
    <!-- Add more profiles as needed -->
</profiles>


mvn clean package -Pprofile1
mvn clean package -Pprofile2



