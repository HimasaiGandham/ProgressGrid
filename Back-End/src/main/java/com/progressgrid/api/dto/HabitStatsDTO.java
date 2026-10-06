package com.progressgrid.api.dto;

import java.util.List;
import java.util.Map;

public class HabitStatsDTO {
    private int totalHabits;
    private int totalCompletions;
    private int overallCompletionPercentage;
    private int currentStreak;
    private int bestStreak;
    private int perfectDays;
    private Map<String, Integer> categoryCounts;
    private Map<String, Integer> categoryCompletionRates;
    private Map<String, Integer> dayOfWeekCompletionRates;
    private List<HabitDTO> topHabits;

    public HabitStatsDTO() {
    }

    public int getTotalHabits() {
        return totalHabits;
    }

    public void setTotalHabits(int totalHabits) {
        this.totalHabits = totalHabits;
    }

    public int getTotalCompletions() {
        return totalCompletions;
    }

    public void setTotalCompletions(int totalCompletions) {
        this.totalCompletions = totalCompletions;
    }

    public int getOverallCompletionPercentage() {
        return overallCompletionPercentage;
    }

    public void setOverallCompletionPercentage(int overallCompletionPercentage) {
        this.overallCompletionPercentage = overallCompletionPercentage;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public int getPerfectDays() {
        return perfectDays;
    }

    public void setPerfectDays(int perfectDays) {
        this.perfectDays = perfectDays;
    }

    public Map<String, Integer> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Integer> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }

    public Map<String, Integer> getCategoryCompletionRates() {
        return categoryCompletionRates;
    }

    public void setCategoryCompletionRates(Map<String, Integer> categoryCompletionRates) {
        this.categoryCompletionRates = categoryCompletionRates;
    }

    public Map<String, Integer> getDayOfWeekCompletionRates() {
        return dayOfWeekCompletionRates;
    }

    public void setDayOfWeekCompletionRates(Map<String, Integer> dayOfWeekCompletionRates) {
        this.dayOfWeekCompletionRates = dayOfWeekCompletionRates;
    }

    public List<HabitDTO> getTopHabits() {
        return topHabits;
    }

    public void setTopHabits(List<HabitDTO> topHabits) {
        this.topHabits = topHabits;
    }
}
