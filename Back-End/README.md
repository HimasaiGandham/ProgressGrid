# ⚙️ ProgressGrid - Backend

This directory contains the robust server-side logic for the ProgressGrid Habit Tracker. It is built using **Java 17** and **Spring Boot 3**, utilizing Spring Data JPA to communicate with the MySQL database.

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
  - `User.java`: User account information.
  - `HabitCategory.java`: Categories for habits (e.g., Health, Work).
  - `Habit.java`: The core habit definition.
  - `HabitCompletion.java`: Tracks the specific dates a habit was successfully checked off.

- **`repository/` (Data Access Layer)**
  Spring Data interfaces that provide automatic CRUD operations for our entities without writing raw SQL. (e.g., `HabitRepository`, `HabitCompletionRepository`).

- **`service/` (Business Logic Layer)**
  - `HabitService.java`: The brain of the backend. It maps database entities into DTOs and handles complex business logic like calculating the **Current Streak**, **Best Streak**, and **Completion Percentages**.

- **`dto/` (Data Transfer Objects)**
  - `HabitDTO.java` & `ToggleCompletionDTO.java`: Plain Java objects used to format data exactly how the frontend expects it, preventing infinite loops or exposing sensitive database columns.

- **`controller/` (API Layer)**
  - `HabitController.java`: The REST API endpoints (`GET /api/habits`, `POST /api/habits`, etc.). It includes `@CrossOrigin` to seamlessly accept requests from the HTML frontend.

## 🚀 How to Run

1. Open this `Back-End` folder as a project in your Java IDE (IntelliJ IDEA, Eclipse, or VS Code).
2. Ensure your local MySQL database is running and the `progressgrid` schema exists.
3. Run the `ProgressGridApplication.java` main class.
4. The server will start on `http://localhost:8080`.
