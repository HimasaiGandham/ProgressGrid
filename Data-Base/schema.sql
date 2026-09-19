-- Creates the database and the MySQL user the backend connects with (see
-- Back-End/src/main/resources/application.properties). Run it as root:
--   mysql -u root -p < Data-Base/schema.sql
-- It's safe to run again: nothing gets dropped, and the backend also adds any
-- missing tables or columns itself when it starts.

CREATE DATABASE IF NOT EXISTS progressgrid_db;
CREATE USER IF NOT EXISTS 'pg_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON progressgrid_db.* TO 'pg_user'@'localhost';
USE progressgrid_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS habit_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#4CAF50'
);

CREATE TABLE IF NOT EXISTS habits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) DEFAULT 'Daily',
    target_days INT DEFAULT 7,
    start_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES habit_categories(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS habit_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    habit_id BIGINT NOT NULL,
    completion_date DATE NOT NULL,
    completed BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_habit_date (habit_id, completion_date),
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);
