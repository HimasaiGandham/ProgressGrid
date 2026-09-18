# ProgressGrid

ProgressGrid is a visual habit tracking and personal progress dashboard built with a Spring Boot 3 Java backend, MySQL persistence, and a modern responsive dark glassmorphic web interface.

---

## Key Highlights

- **User Authentication & Verification**:
  - Secure registration and login with Username or Email.
  - Plain and BCrypt hashed password verification.
  - Session tokens and authenticated user state tracking.
- **Email OTP Verification**:
  - Direct HTTP/2 transactional email integration with **Resend API** (`https://api.resend.com/emails`).
  - Automatic SMTP mail server fallback & console fallback for local development.
  - 6-digit cryptographic numeric codes with 10-minute validity.
  - 3-step security modal with masked email dispatch, 60s resend timer, and password reset.
- **Habit Tracking & Weekly Completion Grid**:
  - Create, customize, and categorize habits (Health, Work, Learning, Fitness, Mindfulness).
  - 7-day completion status grid with real-time toggle checkboxes.
  - Relational database persistence linking users, habits, categories, and completion dates.
- **Progress Analytics & Streaks**:
  - Current streak and all-time best streak calculation engine.
  - Overall completion rate scoring across tracked habits.
  - Interactive Chart.js weekly trend bar chart with smooth animations.
- **UI & Presentation Architecture**:
  - Dark glassmorphic aesthetic with custom CSS variables and neon gradients.
  - Mobile-responsive layout, top brand header, overview cards, and interactive modals.

---

## Directory Structure

```
ProgressGrid/
├── Back-End/              # Spring Boot 3 / Java 17 REST API
│   ├── pom.xml
│   ├── src/main/java/com/progressgrid/api/
│   │   ├── controller/    # AuthController, HabitController
│   │   ├── dto/           # Auth, OTP, ResetPassword, Habit DTOs
│   │   ├── model/         # User, Habit, HabitCategory, HabitCompletion
│   │   ├── repository/    # Spring Data JPA Repositories
│   │   └── service/       # AuthService, OtpService, EmailService, HabitService
│   └── src/main/resources/application.properties
├── Front-End/             # Vanilla HTML, CSS, JavaScript (No build required)
│   ├── login.html         # Login, signup & OTP verification view
│   ├── login.css          # Auth & OTP modal styles
│   ├── login.js           # Auth & OTP handler logic
│   ├── index.html         # Dashboard & habit tracker view
│   ├── style.css          # Design system & dashboard styling
│   ├── app.js             # Habit grid, Chart.js & progress calculations
│   └── logo.png           # Official brand logo
├── Data-Base/             # MySQL Database DDL & Seed Scripts
│   ├── schema.sql         # Table schemas for users, habits, completions
│   └── seed.sql           # Initial category definitions and seed routines
├── dev_server.py          # Python dev server with /api proxy to backend
├── run-app.bat            # Quick startup script for Windows
├── run-app.ps1            # PowerShell automation script
└── README.md
```

---

## Running the Application

### 1. Database Setup
```sql
mysql -u root -p < Data-Base/schema.sql
mysql -u root -p < Data-Base/seed.sql
```

### 2. Start the Backend (Spring Boot)
```bash
cd Back-End
mvn spring-boot:run
```
*Listens on port `8080` (`http://localhost:8080`).*

### 3. Start the Frontend Dev Server
```bash
python dev_server.py
```
*Open `http://localhost:3000` in your browser to access the complete application with live `/api` proxying.*

---

## Feature Branches on GitHub

- **Login Page Verification**: [feature/login-page-verification](https://github.com/HimasaiGandham/ProgressGrid/tree/feature/login-page-verification)
- **Email OTP Verification**: [feature/otp-verification](https://github.com/HimasaiGandham/ProgressGrid/tree/feature/otp-verification)
- **Habit Tracking System**: [feature/habit-tracking](https://github.com/HimasaiGandham/ProgressGrid/tree/feature/habit-tracking)
- **Progress & Streak Analytics**: [feature/progress-tracking](https://github.com/HimasaiGandham/ProgressGrid/tree/feature/progress-tracking)
- **UI Dashboard & Styling**: [feature/ui-dashboard](https://github.com/HimasaiGandham/ProgressGrid/tree/feature/ui-dashboard)
