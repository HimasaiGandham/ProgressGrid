# ProgressGrid - Progress Tracking & Streak Analytics

This branch (`feature/progress-tracking`) introduces real-time habit completion analytics, streak calculation algorithms, and progress visualization charts.

## Features Included
- **Streak Calculation Engine**:
  - **Current Streak**: Determines active contiguous completion chains up to the current date (accounting for grace periods if today is not yet checked).
  - **Best Streak**: Computes historical maximum consecutive completion runs across all recorded completion dates.
- **Completion Rate Scoring**:
  - Computes exact percentage completion based on days elapsed since routine initiation versus recorded completions.
- **Chart.js Progress Visualizations**:
  - Weekly habit completion trend bar chart.
  - Interactive tooltips, responsive canvas rendering, and custom color gradients.
- **Progress Metric Cards**:
  - Total Active Habits, Overall Completion Rate (%), Current Streak, and Best Streak counters.

## Architecture
- **Front-End**:
  - `Front-End/app.js`: Progress calculations, Chart.js dataset generation, DOM counter updates, and animated progress rings.
  - `Front-End/index.html`: Progress analytics metrics section and weekly trend canvas.
  - `Front-End/style.css`: Progress indicators, streak badges, and chart container styling.
- **Back-End (Spring Boot 3 / Java 17)**:
  - `HabitService.java`: `calculateStreaksAndStats` algorithm resolving contiguous calendar intervals and percentage scores.
  - `HabitDTO.java`: Exposes `currentStreak`, `bestStreak`, `completedDays`, and `completionPercentage`.
