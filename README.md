# News Management System

This project is a backend implementation of a **Time Management System**, developed as part of an assignment. It includes APIs for authentication, user management, and time log management, built using **Spring Boot 3.4.2**, **Java 17**, and **MySQL**.

---

## Table of Contents

1. [Technologies Used](#technologies-used)
2. [Security Implementation](#security-implementation)
3. [Database and ORM](#database-and-orm)
4. [Validation](#validation)
5. [API Documentation](#api-documentation)
6. [Project Structure](#project-structure)
7. [Exception Handling](#exception-handling)
8. [Redis Integration](#redis-integration)
9. [Logging with Log4J](#logging-with-log4j)
10. [Unit Testing](#unit-testing)
11. [Setup Instructions](#setup-instructions)

---

## Technologies Used

- **Framework**: Spring Boot 3.4.2
- **Java Version**: JDK 17
- **Database**: MySQL
- **ORM**: Hibernate
- **Security**: Spring Security with JWT (JSON Web Tokens)
- **Password Hashing**: BCryptPasswordEncoder
- **Validation**: Java Bean Validation API
- **API Documentation**: Swagger
- **Build Tool**: Maven
- **Caching**: Redis
- **Logging**: Log4J
- **Testing**: JUnit, Mockito, JaCoCo

---

## Security Implementation

### JWT Authentication

- Implements JWT for securing APIs.
- **Token Time-To-Live (TTL)**: 10 minutes.
- Includes a **refresh token mechanism** valid for 30 minutes.
- **Signature Algorithm**: HS512.
- Only specific endpoints are permitted without authentication;
  signup and login.
- All others require a valid JWT.

### Password Hashing

- Passwords are hashed using the **BCryptPasswordEncoder** algorithm before being stored in the database, ensuring secure authentication and storage.

### Authentication API

### Endpoints

#### 1. POST `/api/auth/signup`
- Registers a new user.

#### 2. POST `/api/auth/login`
- Logs in a user and returns an access token and refresh token.

#### 3. POST `/api/auth/logout`
- Logs out a user.
- Requires the access token in the Authorization header.

#### 4. POST `/api/auth/refresh`
- Refreshes the access token for an extra 1 minute and returns it for the user.
- Requires the refresh token in the Authorization header (valid for 30 minutes).

---

## Database and ORM

- **Database**: MySQL is used to store all entities and data.
- **ORM Framework**: Hibernate is used for interacting with the database.
- Entities are mapped to database tables using **Java Persistence API (JPA)** annotations.

---

## Validation

- Input validation is implemented using the **Java Bean Validation API**.
- Ensures data integrity by validating required fields, data types, and constraints.
- Examples of validations:
  - Validating email format.
  - Ensuring password meets security requirements.
  - Checking for non-null fields like `name` or `role`.

---

## API Documentation

- Swagger is used for API documentation.
- API endpoints are documented and can be tested using Swagger UI.
- **Access URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Redis Integration

- Redis is used to store JWT tokens for session management.
- When a user logs out, their token is added to Redis to prevent reuse.
- This improves security by ensuring that blacklisted tokens are not re-used until they expire.
- Redis can be configured in as in-memory storage

---

## Logging with Log4J

- Log4J is used as the default logging framework.
- Logging guidelines:
  - **INFO level**: Logs function entry points.
  - **DEBUG level**: Logs function parameters and detailed execution steps.
  - **ERROR level**: Logs exceptions in catch blocks.
  - **WARN level**: Logs unexpected results that do not cause system errors.
  - **DEBUG level**: Used as needed for deep debugging.
- Example Log4J configuration in `log4j2.xml`:
  ```<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
  ```

---

## Unit Testing

- Uses **JUnit** and **Mockito** for unit testing.
- Service layer is fully covered with unit tests.
- **JaCoCo** is used for test coverage reporting.
- Run tests with:
  ```sh
  mvn test
  ```
- Generate coverage report:
  ```sh
  mvn jacoco:report
  ```
- Coverage reports are generated in `target/site/jacoco/index.html`.

---

## Spring Boot Application Setup Instructions

1. Clone the repository:  
   `git clone https://github.com/morabicit/timeLog_backend.git`  
   `cd <timeLog_backend>`

2. Ensure the following prerequisites are installed on your system:  
   - Java 17
   - MySQL database server  
   - Redis server  

3. Open the `src/main/resources/application.properties` file and update the following properties with your MySQL database configuration:

   ```properties
   spring.application.name=<your-application-name>
   spring.datasource.url=jdbc:mysql://<your-database-host>:<your-database-port>/<your-database-name>
   spring.datasource.username=<your-database-username>
   spring.datasource.password=<your-database-password>
   spring.jpa.hibernate.ddl-auto=<your-ddl-auto-strategy>
   spring.jpa.show-sql=true
   ```

4. Build the application using Maven and skip tests:
   ```sh
   mvn clean install
   ```

5. Run the application:
   ```sh
   mvn spring-boot:run
   ```

6. Verify the application is running on `http://localhost:8080` and ensure your MySQL and Redis servers are accessible. The application will automatically create or update tables in the database based on the JPA configuration.

---
---

## React Frontend (Time Management System UI)

The frontend for this Time Management System is built using **React** and is available in a separate repository.

### Repository Link:
[GitHub - TimeLog React Frontend](https://github.com/morabicit/timeLog_react)

### Technologies Used:
- **React 18** (Frontend framework)
- **React Router** (Navigation)
- **Axios** (API calls)
- **MUI (Material-UI)** (UI Components)
- **Formik & Yup** (Form handling and validation)
- **JWT Authentication** (Secured API communication)

### Setup Instructions

1. **Clone the frontend repository**:  
   `git clone https://github.com/morabicit/timeLog_react.git && cd timeLog_react`  

2. **Install dependencies**:  
   `npm install`  

3. **Configure API connection**:  
   Set the backend API base URL in the React project:  
   `export const API_BASE_URL = "http://localhost:8080";`  

4. **Start the React development server**:  
   `npm start`  
   The React app will run on [http://localhost:3000](http://localhost:3000).  

5. **Ensure Backend Connection**:  
   - The frontend interacts with the backend system running on port `8080`.  
   - JWT authentication is enforced, ensuring only authenticated users can access protected APIs.  
   - Login and token management are handled using local storage.  

6. **Testing API Integration**:  
   - Ensure the backend (Spring Boot) is running on port `8080`.  
   - Register or log in to retrieve the JWT token.  
   - The token is automatically attached to all API requests to ensure secure access.

