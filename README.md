# 🎨 ProgressGrid - Frontend

This directory contains the user interface and presentation logic for the ProgressGrid Habit Tracking Dashboard. It is built using **Vanilla HTML, CSS, and JavaScript** without any heavy frameworks, ensuring it is lightweight, lightning-fast, and easy to customize.

## 📂 File Structure & Purpose

- **`index.html`**
  The main entry point for the application. It contains the complete structural layout of the dashboard, including the sidebar navigation, summary cards, the dynamic habit grid skeleton, right-side panels (for charts and today's habits), and the "+ Add Habit" modal.

- **`style.css`**
  The global stylesheet. It defines the beautiful pastel design system, responsive flexbox/grid layouts, dynamic hover effects, and custom CSS variables (like the heatmap completion colors `--c-0` through `--c-4`).

- **`app.js`**
  The core frontend application logic. It is responsible for:
  - **Fetching Data**: Communicating with the Spring Boot backend (`http://localhost:8080/api/habits`).
  - **Dynamic Rendering**: Injecting the habits into the grid and updating the dates dynamically based on the current week.
  - **Interactivity**: Handling user clicks on checkboxes (optimistic UI updates) and form submissions for creating new habits.
  - **Calculations & Visuals**: Aggregating completion percentages, streaks, and rendering the Weekly Progress graph using **Chart.js**.

## 🚀 How to Run

1. Ensure the Spring Boot backend is running.
2. Simply double-click `index.html` to open it in your default web browser! No local server or Node.js environment is required for the frontend.
