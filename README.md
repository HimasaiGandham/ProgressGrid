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
