# ⚙️ ProgressGrid - Backend

<p align="left">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA" />
  <img src="https://img.shields.io/badge/MySQL-Connector-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
</p>

This directory contains the robust server-side logic for the ProgressGrid platform. It is built using **Java 17** and **Spring Boot 3**, utilizing Spring Data JPA to communicate with the MySQL database.

---

## 📂 Architecture & File Structure

The project is structured using standard Spring Boot layered architecture:

- **`pom.xml`**
  The Maven configuration file. It manages all dependencies (Spring Web, Spring Data JPA, MySQL Connector) and plugins required to build the application.

- **`src/main/resources/application.properties`**
  The core configuration file. It defines the database connection URL, credentials, Hibernate DDL settings, and the local server port (`8080`).

- **`src/main/java/com/progressgrid/api/`** (Root Package)
  - **`ProgressGridApplication.java`**: The main class that bootstraps and launches the Spring Boot application.

### 📦 Sub-Packages

- **`model/` (JPA Entities)**
  Contains the database models mapped via Hibernate:
  - `User.java`: User account information and authentication credentials.
  - `HabitCategory.java`: Categories for habits (e.g., Health, Work, Study).
  - `Habit.java`: The core habit/task definition.
  - `HabitCompletion.java`: Tracks the specific dates an activity was successfully checked off.

- **`repository/` (Data Access Layer)**
  Spring Data interfaces that provide automatic CRUD operations for entities without writing raw SQL (`UserRepository`, `HabitRepository`, `HabitCompletionRepository`, `HabitCategoryRepository`).

- **`service/` (Business Logic Layer)**
  - `HabitService.java`: Maps database entities into DTOs and handles core business logic including **Current Streak**, **Best Streak**, and **Completion Percentages**.
  - `AuthService.java`: Manages user registration, password verification, and authentication tokens.
  - `EmailService.java` & `OtpService.java`: Coordinates transactional email OTP generation and validation for password recovery.

- **`dto/` (Data Transfer Objects)**
  - `HabitDTO.java`, `ToggleCompletionDTO.java`, `LoginDTO.java`, `VerifyOtpDTO.java`: Plain Java objects used to format data cleanly across the REST API, preventing infinite loops or exposing sensitive database columns.

- **`controller/` (REST API Layer)**
  - `HabitController.java`: The REST API endpoints (`GET /api/habits`, `POST /api/habits`, etc.). Includes `@CrossOrigin` to seamlessly accept requests from the web frontend.
  - `AuthController.java`: Authentication and OTP verification endpoints (`POST /api/auth/login`, `POST /api/auth/send-otp`, etc.).

---

## 🚀 How to Run

1. Open this `Back-End` folder as a project in your Java IDE (IntelliJ IDEA, Eclipse, or VS Code).
2. Ensure your local MySQL database is running and the `progressgrid` schema exists.
3. Run the `ProgressGridApplication.java` main class or use Maven:
   ```bash
   mvn spring-boot:run
   ```
4. The server will start on `http://localhost:8080`.
