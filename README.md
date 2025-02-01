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
8. [Setup Instructions](#setup-instructions)

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

---

## Security Implementation

### JWT Authentication

- Implements JWT for securing APIs.
- **Token Time-To-Live (TTL)**: 10 minute.
- Includes a **refresh token mechanism** valid for 30 minutes.
- **Signature Algorithm**: HS512.
- Only specific endpoints are permitted without authentication;
  signup and login.
-  all others require a valid JWT.

### Password Hashing

- Passwords are hashed using the **BCryptPasswordEncoder** algorithm before being stored in the database, ensuring secure authentication and storage.
- 
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
- Refreshes the access token for an extra 1 minute and return it for the user.  
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

## Project Structure

- **Security Configurations**: Handles API authentication, implement filters, and generate/validate JWT tokens.
- **Controller Layer**: Handles API endpoints for authentication, user management, and time log operations.
- **Service Layer**: Contains business logic for user and time logs workflows.
- **Repository Layer**: Manages database access using Hibernate and JPA.
- **Entities**: Represent database tables and include mappings for relationships.
- **Enums**: Define roles.
- **Exception Handling**: Includes a global exception handling mechanism.

---

## Role-Based Access Control

### Roles:

1. **USER**:
   - Can access authentication APIs and view news.

### Default Role:

- If no role is specified during registration, the default role is `USER`.
Note: no need to implement many roles as the specification of this assessment
---

## Exception Handling

- Implements a global exception handling mechanism using **Spring Rest Controller Advice**.
- Custom exceptions are handled to provide meaningful error messages.
- Validation errors are processed and return detailed messages with field-specific issues.

---

# Spring Boot Application Setup Instructions

1. Clone the repository:  
   `git clone [https://github.com/morabicit/wave.git](https://github.com/morabicit/timeLog_backend.git)`  
   `cd <repository-name>`

2. Ensure the following prerequisites are installed on your system:  
   - Java 17
   - MySQL database server  

3. Open the `src/main/resources/application.properties` file and update the following properties with your MySQL database configuration:

   ```properties
   spring.application.name=<your-application-name>
   spring.datasource.url=jdbc:mysql://<your-database-host>:<your-database-port>/<your-database-name>
   spring.datasource.username=<your-database-username>
   spring.datasource.password=<your-database-password>
   spring.jpa.hibernate.ddl-auto=<your-ddl-auto-strategy>
   spring.jpa.show-sql=true```
   
4. Build the application using Maven and skip test:  
`mvn clean install`

5. Run the application:  
`mvn spring-boot:run`

6. Verify the application is running on `http://localhost:8080` and ensure your MySQL database is accessible. The application will automatically create or update tables in the database based on the JPA configuration.
