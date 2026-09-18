# 🚀 ProgressGrid – Project Introduction

<p align="center">
  <img src="Front-End/logo.png" alt="ProgressGrid Logo" width="120" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" alt="JavaScript" />
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML5" />
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" alt="CSS3" />
</p>

**ProgressGrid** is a web-based **project and task management system** designed to help teams plan, organize, assign, and monitor their work from a single centralized platform. 

In many projects, tracking tasks through spreadsheets, messages, and manual updates can make it difficult to understand **who is responsible for a task, what has been completed, and what still needs attention**. Progress Grid addresses this problem by providing a **visual and organized workspace** where project activities can be monitored easily.

The system uses a **progress grid and dashboard-based approach** to display task status, team responsibilities, deadlines, and overall project completion. Team members can update their assigned tasks, while project coordinators can get a clear overview of the team's progress.

By bringing project information into one platform, Progress Grid aims to improve **visibility, accountability, collaboration, and productivity**. ⚡ It provides a simple foundation that can also be extended with advanced features such as notifications, analytics, reports, role-based access, and performance tracking.

---

## 🌟 Key Features

- **Visual Progress Tracking** – Monitor project completion through an intuitive progress grid.
- **Task Management** – Create, update, prioritize, and manage project tasks and daily activities.
- **Team Assignment & Ownership** – Assign tasks and responsibilities to specific team members.
- **Status Updates** – Track tasks as pending, in progress, or completed with real-time feedback.
- **Deadline & Streak Tracking** – Monitor upcoming deadlines and calculate consistency streaks.
- **Progress Dashboard** – Get a quick overview of overall project and team progress with interactive charts.
- **Accountability & Transparency** – Clearly identify task ownership and current status.
- **Centralized Data Management** – Keep project, task, and team information organized in one place.
- **Email OTP & Security** – Secure account registration, authentication, and password reset flows.
- **Improved Collaboration** – Help team members coordinate their work more effectively.
- **Scalable Architecture** – Clean separation between Spring Boot backend, MySQL database, and frontend dashboard.

---

## 💻 Technology Stack

| Layer | Technologies | Details |
|---|---|---|
| **Backend** | **Java 17, Spring Boot 3** | RESTful APIs, Spring Data JPA, Hibernate, Spring Security |
| **Database** | **MySQL 8.0** | Relational schemas, foreign keys, cascading deletions, seed routines |
| **Frontend** | **JavaScript (ES6+), HTML5, CSS3** | Dynamic responsive glassmorphic dashboard, Chart.js analytics |

---

## 🎯 Project Goal

The goal of **Progress Grid** is to provide teams with a **simple, visual, and centralized platform** to manage project activities, understand responsibilities, track completion, and identify pending work.

---

## 📂 Directory Structure

```
ProgressGrid/
├── Back-End/              # Spring Boot 3 & Java 17 REST API
│   ├── pom.xml            # Maven configuration and dependencies
│   ├── src/main/java/     # Application controllers, models, DTOs, services, repositories
│   └── src/main/resources/# application.properties database configuration
├── Front-End/             # Vanilla Web Client (HTML5, CSS3, JavaScript)
│   ├── index.html         # Main dashboard & interactive progress grid view
│   ├── style.css          # Design system & dark glassmorphic styling
│   ├── app.js             # Dynamic grid rendering, Chart.js & progress calculations
│   ├── login.html         # Authentication & email OTP verification view
│   ├── login.css          # Login & security modal stylesheet
│   ├── login.js           # Auth handler logic & OTP verification
│   └── logo.png           # Brand logo asset
├── Data-Base/             # MySQL Database DDL & Initialization
│   ├── schema.sql         # Relational database table schemas
│   └── seed.sql           # Initial category definitions and seed records
├── dev_server.py          # Python dev server with live /api proxy to backend
├── run-app.bat            # Windows startup script
└── run-app.ps1            # PowerShell automation script
```

---

## 🚀 Running the Application

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

## 📖 Module Documentation

- [Back-End Documentation](Back-End/README.md)
- [Front-End Documentation](Front-End/README.md)
- [Data-Base Documentation](Data-Base/README.md)
