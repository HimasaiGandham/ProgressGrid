package com.progressgrid.backend.dto;

import java.util.Map;

public class ProgressDto {

    private int dailyPercentage;
    private int weeklyAveragePercentage;
    private int monthlyPercentage;

    private Map<String, Integer> weeklyData; // For bar chart (Day -> Percentage)

    private int completedActivities;
    private int plannedActivities;

    // Getters and Setters

    public int getDailyPercentage() { return dailyPercentage; }
    public void setDailyPercentage(int dailyPercentage) { this.dailyPercentage = dailyPercentage; }
    public int getWeeklyAveragePercentage() { return weeklyAveragePercentage; }
    public void setWeeklyAveragePercentage(int weeklyAveragePercentage) { this.weeklyAveragePercentage = weeklyAveragePercentage; }
    public int getMonthlyPercentage() { return monthlyPercentage; }
    public void setMonthlyPercentage(int monthlyPercentage) { this.monthlyPercentage = monthlyPercentage; }
    public Map<String, Integer> getWeeklyData() { return weeklyData; }
    public void setWeeklyData(Map<String, Integer> weeklyData) { this.weeklyData = weeklyData; }
    public int getCompletedActivities() { return completedActivities; }
    public void setCompletedActivities(int completedActivities) { this.completedActivities = completedActivities; }
    public int getPlannedActivities() { return plannedActivities; }
    public void setPlannedActivities(int plannedActivities) { this.plannedActivities = plannedActivities; }
}
