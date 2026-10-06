# 📊 ProgressGrid

> **Track Your Progress. Improve Every Day.**

ProgressGrid is a **personal daily habit and progress-tracking web application designed mainly for students**. It helps users organize their day-to-day activities, maintain consistency, and visually understand how well they are progressing throughout the day, week, and month.

---
🚀 Live Demo: https://himasaigandham.github.io/ProgressGrid/login.html

## 🎯 Why ProgressGrid?

Students often decide to complete several activities during a particular day, but unexpected work, distractions, or other situations can prevent them from completing everything as planned.

ProgressGrid is designed to solve this problem by giving students a simple place to record their daily habits and activities and continuously track their completion.

Instead of simply writing down a list of tasks, ProgressGrid makes progress **visible and measurable**.

The goal is to help students:

* Build consistency
* Manage their daily activities better
* Maintain balance throughout the day
* Identify areas where they are falling behind
* Understand their progress over time
* Stay motivated through visual progress tracking

---

## 💡 What is ProgressGrid?

ProgressGrid works as a **personal habit and activity dashboard**.

Users can enter the habits or activities they want to complete and track them using an interactive grid.

Each activity is represented by a box in the grid. When an activity is completed, the user can tick the corresponding box.

The application then converts these completed activities into visual progress results.

### Example

A student might track activities such as:

* 📚 Study
* 💻 Coding
* 📝 Assignments
* 🏃 Exercise
* 📖 Reading
* 🧠 Skill development
* 🎯 Personal activities

The exact activities are completely customizable according to the user's needs.

---

# 🖥️ Dashboard

The dashboard is the main interface of ProgressGrid.

The interface is designed to keep important information easily accessible without making the application feel complicated.

### Dashboard Layout

The dashboard contains:

* **App Logo** — positioned in the upper-left corner
* **Dashboard** — the main workspace
* **Habit/Activity Grid** — where users enter and track their activities
* **Daily Progress** — displayed as a circular progress indicator
* **Weekly Progress** — displayed using a bar graph
* **Monthly Progress** — displayed as an overall progress indicator
* **Notifications** — generated to remind users about activities

The dashboard provides a quick overview of the user's current progress.

---

# 📅 Activity & Habit Tracking

The core of ProgressGrid is its activity-tracking grid.

Users can add their own day-to-day activities and habits.

Each activity is represented using a separate box in the grid.

Users can simply **tick the respective box when an activity is completed**.

This makes tracking quick and simple without requiring complicated data entry.

---

# 📈 Progress Visualization

ProgressGrid transforms completed activities into visual information so users can understand their consistency more easily.

## 🟢 Daily Progress

Daily progress is calculated based on the activities completed during the day.

The result is displayed using a **circular progress indicator**.

For example:

```text
        DAILY PROGRESS

           75%
        ╭───────╮
       │       │
       │   ✓   │
       │       │
        ╰───────╯
```

The percentage represents the proportion of planned activities that were completed.

---

## 📊 Weekly Progress

Weekly progress provides a broader view of consistency.

The application records the user's daily completion results throughout the week and displays them using a **bar graph**.

Example:

```text
Weekly Progress

100% |       █
 80% |   █   █       █
 60% |   █   █   █   █
 40% | █ █   █   █   █
 20% | █ █   █   █   █
     +-------------------
       Mon Tue Wed Thu Fri
```

This allows users to quickly identify:

* Strong days
* Weak days
* Consistency throughout the week
* Changes in performance

---

## 📅 Monthly Progress

ProgressGrid also provides a monthly overview.

The monthly result is calculated from the user's daily completion performance across the month.

A simple and fair approach is:

**Monthly Progress = Total completed activities ÷ Total planned activities × 100**

For example, if 180 activities were planned during a month and 144 were completed:

**Monthly Progress = 144 ÷ 180 × 100 = 80%**

The resulting percentage can be displayed on the right side of the dashboard as the user's **Monthly Progress**.

This gives users a quick understanding of their overall consistency during the month.

---

# 🔔 Notifications

ProgressGrid includes notifications to help users stay aware of their activities.

Notifications can be generated for situations such as:

* Pending activities
* Incomplete activities
* Daily activity reminders
* Important tracking updates

The purpose of notifications is not to overwhelm the user, but to help them remember activities they intended to complete.

---

# 📆 Multiple Time Views

ProgressGrid allows users to understand their progress across different time periods:

### Daily

Shows the user's progress for the current day through a circular progress indicator.

### Weekly

Shows progress across the week using a bar graph.

### Monthly

Provides an overall monthly progress percentage based on activity completion.

This allows users to understand both **short-term performance and long-term consistency**.

---

# ⭐ Core Idea

ProgressGrid is **not just a task-list application**.

Traditional task managers mainly answer:

> "What do I need to do?"

ProgressGrid focuses on:

> **"How consistently am I actually doing it?"**

The system turns everyday activities into measurable progress so that users can see their consistency rather than simply seeing a list of unfinished tasks.

---

# 🎯 Main Objective

The main objective of ProgressGrid is to help students:

**Track their daily activities → Maintain consistency → Visualize their progress → Identify areas where they fall behind → Improve over time.**

The application is intended for **personal use**, rather than being a college administration, teacher-management, or institutional system.

---

# 🛠️ Technology Stack

The planned technology stack for ProgressGrid includes:

### Frontend

* HTML
* CSS
* JavaScript

The frontend is responsible for:

* Dashboard interface
* Activity grid
* Checkboxes
* Progress indicators
* Charts
* Notifications
* User interaction

### Backend

* Java
* Python

The backend layer can handle:

* User data
* Activity information
* Progress calculations
* Notification logic
* Communication between the interface and database

### Development Environment

* **Gemini IDE**

The project is developed with the help of Gemini IDE for implementation, development assistance, and project building.

---

# 🏗️ Basic System Architecture

```text
                    ┌─────────────────────┐
                    │      ProgressGrid   │
                    │      Dashboard      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │      Frontend       │
                    │ HTML / CSS / JS     │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │       Backend       │
                    │    Java / Python    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     Data Storage    │
                    │ Activities / Habits │
                    │      Progress       │
                    └─────────────────────┘
```

---

# 🔄 How ProgressGrid Works

```text
1. User opens ProgressGrid
              ↓
2. User enters daily activities
              ↓
3. Activities appear in the grid
              ↓
4. User completes an activity
              ↓
5. User ticks the corresponding box
              ↓
6. Progress is calculated automatically
              ↓
7. Daily progress → Circular indicator
              ↓
8. Weekly progress → Bar graph
              ↓
9. Monthly progress → Overall percentage
              ↓
10. User understands their consistency
```

---

# 📊 Progress Calculation

ProgressGrid primarily measures **completion consistency**.

### Daily Progress

```text
Daily Progress =
Completed Activities / Planned Activities × 100
```

### Weekly Progress

Weekly progress is represented using the daily completion percentages for each day of the week.

### Monthly Progress

```text
Monthly Progress =
Total Completed Activities /
Total Planned Activities × 100
```

This approach ensures that the progress shown by the application is based on the user's actual activity completion.

---

# 🌱 Project Philosophy

ProgressGrid is built around a simple idea:

> **Small actions completed consistently create meaningful progress.**

Missing an activity on one day does not define the user's overall performance.

Instead, ProgressGrid allows users to look at their progress across multiple days and understand their overall consistency.

The purpose is to encourage **awareness, balance, consistency, and continuous improvement**.

---

# ▶️ Running ProgressGrid

ProgressGrid has three parts: the static site in `Front-End/`, the Spring Boot API in `Back-End/` (Java 17) and a MySQL database (`Data-Base/`).

## Locally

**Quick start (Windows):** run `run-app.bat`. It starts the API on `http://localhost:8080` and `dev_server.py` on `http://localhost:3000`, which serves the site and forwards `/api` to the API. You need Java 17+, Maven and Python 3.

**Without MySQL:** start the API with the in-memory H2 profile. Data is lost when it stops.

```bash
cd Back-End
mvn spring-boot:run -Dspring-boot.run.profiles=dev
python ../dev_server.py        # in another terminal, then open http://localhost:3000
```

**With MySQL:** create the database and user first (`mysql -u root -p < Data-Base/schema.sql`), then run `mvn spring-boot:run` without a profile.

## Where the site sends its requests

`Front-End/config.js` (loaded by both pages) picks the API address from the page's own address:

| Site opened from | API used |
|---|---|
| port `3000` or `8080` (`dev_server.py`, or the API itself) | same address, `/api` |
| `localhost` / `127.0.0.1` on any other port (e.g. VS Code Live Server on `5500`) | `http://localhost:8080/api` |
| `*.github.io` (GitHub Pages) | **none**: demo mode, see below |
| any other host | same address, `/api` |

## ⚠️ GitHub Pages runs in demo mode only

`.github/workflows/deploy.yml` publishes `Front-End/` to GitHub Pages on every push to `main`. GitHub Pages only serves static files, so it **cannot run the Java API or host MySQL**. On a `*.github.io` address the site never calls an API and runs in an offline demo mode:

- sign-up, sign-in and password reset are simulated in the browser (the demo reset code is `123456`);
- habits and ticks are saved in that browser's `localStorage` only. They aren't synced between devices or browsers, and clearing site data deletes them;
- streaks and percentages are rough client-side estimates, not the server's scoring.

Use the [live Pages site](https://himasaigandham.github.io/ProgressGrid/login.html) as a UI preview. Don't use it for real accounts or data.

**Demo mode can also switch on outside GitHub Pages.** On any host, `login.js` quietly falls back to demo sign-in and sign-up when the API answers `404`/`405` or can't be reached, and the reset form accepts `123456` at the code step without asking the API. So a deployment whose `/api` isn't routed correctly still looks as if sign-in works, but nothing is saved on the server. If habits don't sync between browsers, or the session token in `localStorage` starts with `demo-`, the site can't reach `/api`.

## Full-stack deployment

For real accounts and data, host the API and a MySQL database, and serve the site **from the same address as the API**. On any host other than `localhost` and `github.io`, the site calls `/api` on its own origin.

1. **Database:** create a MySQL database on any provider (Railway, Aiven, your own server, …). The API creates the tables on first start.
2. **Site + API in one app:** copy the site into Spring Boot's static folder so the API serves both:
   ```bash
   mkdir -p Back-End/src/main/resources/static
   cp -r Front-End/* Back-End/src/main/resources/static/
   cd Back-End && mvn -DskipTests package   # produces target/tracker-backend-1.0.0-SNAPSHOT.jar
   ```
   Open `/login.html` on the deployed address to sign in.
3. **Host the jar** on any Java 17 host (Railway, Render, Fly.io, a VM, …): `java -jar target/tracker-backend-1.0.0-SNAPSHOT.jar`, listening on port 8080 (set `SERVER_PORT` if the platform assigns a port). Some platforms (e.g. Render, Fly.io) run Docker images; this repo doesn't include a Dockerfile yet.
4. **Set the environment variables** below. `JWT_SECRET` and the datasource settings are required in production.

Alternatively, keep the site on another static host and put a reverse proxy in front of both, so that `/api/*` on the site's address is forwarded to the API. That's what `dev_server.py` does locally. The site can't call an API on a *different* origin: except on `localhost`, it always uses its own.

## Environment variables

Read by `Back-End/src/main/resources/application.properties`:

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | *(none)* | `dev` = in-memory H2 instead of MySQL |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/progressgrid_db?...` | Database JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `pg_user` / `password123` | Database login (**change in production**) |
| `SPRING_DATASOURCE_DRIVER` | `com.mysql.cj.jdbc.Driver` | JDBC driver class |
| `JWT_SECRET` | *(none)*: a random key is generated once and kept in `JWT_SECRET_FILE` | Signs login tokens. **Required in production**: 32+ random characters |
| `JWT_SECRET_FILE` | `~/.progressgrid/jwt-secret` | Where the generated key is kept when `JWT_SECRET` isn't set, so sessions survive restarts |
| `JWT_EXPIRATION_MS` | `86400000` (24 h) | How long a login lasts |
| `RESEND_API_KEY` / `RESEND_FROM_EMAIL` | *(none)* / `ProgressGrid <onboarding@resend.dev>` | Send password reset emails through [Resend](https://resend.com) |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | `smtp.gmail.com` / `587` / *(none)* / *(none)* | SMTP fallback for reset emails |
| `OTP_LOG_CODES` | `false` (`true` in the `dev` profile) | Print reset codes in the backend log when no email provider delivers them. **Never enable in production**: anyone who can read the log could reset any account |

---

# 🚀 Project Status

**Status:** 🚧 In Development

ProgressGrid is currently being developed as a personal-use project.

The core concept focuses on:

* Personal activity tracking
* Habit management
* Daily progress visualization
* Weekly progress visualization
* Monthly progress tracking
* Notifications
* Simple and understandable dashboard design

---

# 📌 Project Scope

ProgressGrid is intentionally designed as a **personal productivity and habit-tracking application**.

It is **not intended to function as:**

* A college management system
* A teacher management system
* An institutional academic portal
* A student administration platform

Its primary purpose is to help an individual **manage, track, and understand their own progress**.

---

# ❤️ Vision

ProgressGrid aims to turn everyday intentions into visible progress.

Instead of forgetting what was planned for the day, users can record their activities, complete them throughout the day, and look back at their progress through simple visualizations.

### **Track it. Complete it. Visualize it. Improve it.**

---

## 👨‍💻 Project

**Project Name:** ProgressGrid
**Project Type:** Personal Habit & Progress Tracking Application
**Primary Users:** Students / Individual Users
**Development Environment:** Gemini IDE
