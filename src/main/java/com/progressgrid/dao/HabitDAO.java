package com.progressgrid.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {

    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT h.id, h.name, c.completion_date FROM habits h LEFT JOIN habit_completions c ON h.id = c.habit_id AND c.is_completed = TRUE AND MONTH(c.completion_date) = 1 AND YEAR(c.completion_date) = 2026 ORDER BY h.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int currentId = -1;
            Habit currentHabit = null;

            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != currentId) {
                    currentId = id;
                    currentHabit = new Habit(id, rs.getString("name"), new ArrayList<>());
                    habits.add(currentHabit);
                }
                
                Date compDate = rs.getDate("completion_date");
                if (compDate != null) {
                    // Extract day of month for January 2026
                    int day = compDate.toLocalDate().getDayOfMonth();
                    currentHabit.getCompletions().add(day);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return habits;
    }

    public void toggleCompletion(int habitId, int day, boolean isCompleted) {
        String dateStr = "2026-01-" + String.format("%02d", day);
        String sql = "INSERT INTO habit_completions (habit_id, completion_date, is_completed) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE is_completed = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, habitId);
            stmt.setString(2, dateStr);
            stmt.setBoolean(3, isCompleted);
            stmt.setBoolean(4, isCompleted);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
