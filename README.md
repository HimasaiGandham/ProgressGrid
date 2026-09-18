# ProgressGrid - Login Page & User Authentication

This branch (`feature/login-page-verification`) implements the dedicated user authentication subsystem for ProgressGrid.

## Features Included
- **User Authentication**: Secure Sign-in via Username or Email with password verification.
- **Account Registration**: Seamless Sign-up with username, email, and minimum 6-character password constraint.
- **Token Handling**: JWT-compatible token generation and client-side session persistence.
- **Modern Login UI**: Responsive, glassmorphic auth cards, dynamic tab switching, password visibility toggles, and toast alerts.

## Architecture
- **Front-End**:
  - `Front-End/login.html`: High-aesthetic dark glassmorphism authentication view.
  - `Front-End/login.css`: Design tokens, glow effects, micro-interactions, responsive inputs.
  - `Front-End/login.js`: Client-side validation, async REST communication, session management.
- **Back-End (Spring Boot 3 / Java 17)**:
  - `AuthController.java`: Clean endpoints (`/api/auth/login`, `/api/auth/signup`, `/api/auth/me`).
  - `AuthService.java`: User credential verification (plain & BCrypt compatibility), registration uniqueness enforcement.
  - `User.java`: JPA entity for user accounts.
  - `UserRepository.java`: Spring Data JPA repository for user lookup by username or email.
- **Data-Base**:
  - `Data-Base/schema.sql`: DDL for `users` schema.
