-- ProgressGrid Database Setup Script
-- Please run this script as a user with sufficient privileges (e.g., 'root').
-- You can run this from MySQL Workbench or the command line:
-- mysql -u root -p < database_setup.sql

CREATE DATABASE IF NOT EXISTS progressgrid_db;

-- Create the user 'pg_user' with password 'password123'
-- Change 'password123' to a more secure password in a production environment
CREATE USER IF NOT EXISTS 'pg_user'@'localhost' IDENTIFIED BY 'password123';

-- Grant all privileges on the database to the new user
GRANT ALL PRIVILEGES ON progressgrid_db.* TO 'pg_user'@'localhost';

FLUSH PRIVILEGES;
