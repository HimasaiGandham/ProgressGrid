-- Sample data for a fresh database: run schema.sql first, then this once.
-- Sign in as admin with the password dummy_hash; it's stored as a proper hash after that first sign-in.
USE progressgrid_db;

-- Default User
INSERT INTO users (username, email, password_hash) 
VALUES ('admin', 'admin@progressgrid.com', 'dummy_hash');

-- Default Categories
INSERT INTO habit_categories (name, color) VALUES 
('Health', '#4CAF50'),
('Fitness', '#F44336'),
('Personal', '#2196F3'),
('Work', '#FF9800'),
('Study', '#9C27B0');

-- Default Habits
INSERT INTO habits (user_id, category_id, name, description, frequency, target_days, start_date) VALUES 
(1, 1, 'Drink Water', 'Drink 8 glasses of water', 'Daily', 7, '2026-09-01'),
(1, 2, 'Exercise', '1 hour gym session', 'Daily', 7, '2026-09-01'),
(1, 3, 'Meditation', '15 mins mindfulness', 'Daily', 7, '2026-09-01'),
(1, 4, 'Coding', 'Work on side projects', 'Daily', 7, '2026-09-01'),
(1, 5, 'Read Book', 'Read 20 pages', 'Daily', 7, '2026-09-01');

-- Sample Completions (Simulating some checkmarks)
INSERT INTO habit_completions (habit_id, completion_date, completed) VALUES
-- Drink Water (Completed almost every day)
(1, '2026-09-01', true), (1, '2026-09-02', true), (1, '2026-09-03', true), (1, '2026-09-04', true),
(1, '2026-09-05', true), (1, '2026-09-06', true), (1, '2026-09-07', true),
-- Exercise (Missed some days)
(2, '2026-09-01', true), (2, '2026-09-03', true), (2, '2026-09-04', true),
(2, '2026-09-06', true), (2, '2026-09-07', true),
-- Meditation
(3, '2026-09-02', true), (3, '2026-09-03', true), (3, '2026-09-05', true),
-- Coding
(4, '2026-09-01', true), (4, '2026-09-02', true), (4, '2026-09-03', true), (4, '2026-09-04', true),
(4, '2026-09-05', true), (4, '2026-09-06', true), (4, '2026-09-07', true),
-- Read Book
(5, '2026-09-01', true), (5, '2026-09-02', true), (5, '2026-09-04', true), (5, '2026-09-06', true);
