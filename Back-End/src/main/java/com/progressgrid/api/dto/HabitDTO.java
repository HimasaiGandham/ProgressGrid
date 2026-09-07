package com.progressgrid.api.dto;

import java.time.LocalDate;
import java.util.List;

public class HabitDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String frequency;
    private Integer targetDays;
    private LocalDate startDate;
    private List<LocalDate> completions;
    
    // Streaks and Stats
    private Integer currentStreak;
    private Integer bestStreak;
    private Integer completedDays;
    private Integer completionPercentage;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public Integer getTargetDays() { return targetDays; }
    public void setTargetDays(Integer targetDays) { this.targetDays = targetDays; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public List<LocalDate> getCompletions() { return completions; }
    public void setCompletions(List<LocalDate> completions) { this.completions = completions; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
    public Integer getBestStreak() { return bestStreak; }
    public void setBestStreak(Integer bestStreak) { this.bestStreak = bestStreak; }
    public Integer getCompletedDays() { return completedDays; }
    public void setCompletedDays(Integer completedDays) { this.completedDays = completedDays; }
    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
}
