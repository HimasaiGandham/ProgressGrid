# ProgressGrid - Habit Tracking & Management

This branch (`feature/habit-tracking`) implements the core habit tracking system, including categorization, weekly completion grid, toggles, and relational database persistence.

## Features Included
- **Habit Lifecycle Management**: Create, edit, list, and delete habits linked to user accounts.
- **Categorization**: Color-coded habit categories (Health, Work, Learning, Fitness, Mindfulness).
- **Weekly Completion Grid**: 7-day visual status grid with instantaneous checkmark toggling.
- **Relational Persistence**: MySQL schema backing `habits`, `habit_categories`, and `habit_completions`.

## Architecture
- **Front-End**:
  - `Front-End/app.js`: Dynamic habit grid generation, completion day toggling, modal creation workflows, and real-time state synchronization.
  - `Front-End/index.html`: Weekly habit grid table markup and habit creation modal.
  - `Front-End/style.css`: Grid layout styling, custom checkboxes, badge tags, and transitions.
- **Back-End (Spring Boot 3 / Java 17)**:
  - `HabitController.java`: Endpoints for habit listing, creation, and day toggles (`/api/habits`, `/api/habits/{id}/toggle-day`, `/api/habits/categories`).
  - `HabitService.java`: Business logic managing habit records, category mappings, and completion entities.
  - JPA Models: `Habit.java`, `HabitCategory.java`, `HabitCompletion.java`.
  - Repositories: `HabitRepository.java`, `HabitCategoryRepository.java`, `HabitCompletionRepository.java`.
- **Data-Base**:
  - `Data-Base/schema.sql`: DDL for habits, categories, and completion tracking.
  - `Data-Base/seed.sql`: Seed data for default habit categories and sample routines.
