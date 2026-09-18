# 🗄️ ProgressGrid - Database

<p align="left">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/SQL-DDL%20%26%20DML-orange?style=for-the-badge" alt="SQL" />
</p>

This directory contains the foundational SQL scripts required to set up the MySQL database for the ProgressGrid platform. It provides the relational schema definitions and initial mock data needed for local development and testing.

---

## 📂 File Structure & Purpose

- **`schema.sql`**
  This script contains the raw SQL commands to define the relational database structure:
  - Creates the `progressgrid` database cleanly.
  - Creates core tables: `users`, `habit_categories`, `habits`, and `habit_completions`.
  - Establishes Primary Keys, Foreign Keys, unique constraints, and cascading deletion rules (e.g. deleting a habit automatically deletes its completions).
  - Sets up audit timestamps (`created_at`, `updated_at`).

- **`seed.sql`**
  This script populates the database with realistic sample data:
  - A default user account.
  - 5 default habit categories with associated hex theme colors (Health, Fitness, Personal, Work, Study).
  - Sample habits belonging to the user.
  - A historical log of `habit_completions` simulating checked-off days so that the frontend charts and streak calculations have real data to process immediately upon first load.

---

## 🚀 How to Use

1. Open your MySQL client (e.g., **MySQL Workbench** or command line).
2. Execute **`schema.sql`** to build the database structure:
   ```bash
   mysql -u root -p < schema.sql
   ```
3. Execute **`seed.sql`** to populate it with starter data:
   ```bash
   mysql -u root -p < seed.sql
   ```
4. Your Spring Boot backend is now ready to connect.
