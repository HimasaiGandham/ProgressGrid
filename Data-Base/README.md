# 🗄️ ProgressGrid - Database

This directory contains the foundational SQL scripts required to set up the MySQL database for the ProgressGrid application. It provides the schema definitions and initial mock data needed for local development and testing.

## 📂 File Structure & Purpose

- **`schema.sql`**
  This script contains the raw SQL commands to define the relational database structure. It handles:
  - Dropping and recreating the `progressgrid` database cleanly.
  - Creating the core tables: `users`, `habit_categories`, `habits`, and `habit_completions`.
  - Establishing Primary Keys, Foreign Keys, and cascading deletion rules (so deleting a habit automatically deletes its completions).
  - Setting up timestamps (`created_at`, `updated_at`) for audit tracking.

- **`seed.sql`**
  This script populates the database with initial, realistic mock data so that you don't start with an empty dashboard. It injects:
  - A default admin user.
  - 5 default habit categories with associated hex colors (Health, Fitness, Personal, Work, Study).
  - 5 sample habits belonging to the user.
  - A historical log of `habit_completions` simulating checked-off days so that the frontend charts and streak calculations have real data to process immediately upon first load.

## 🚀 How to Use

1. Open your MySQL client (such as **MySQL Workbench** or command line).
2. Execute the entire contents of **`schema.sql`** to build the empty database structure.
3. Execute the entire contents of **`seed.sql`** to populate it with test data.
4. Your Spring Boot backend is now ready to connect to it!
