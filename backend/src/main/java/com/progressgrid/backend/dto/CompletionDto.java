package com.progressgrid.backend.dto;

import java.time.LocalDate;

/** One tick in the weekly grid: which activity, which day, done or not. */
public class CompletionDto {
    private Long activityId;
    private LocalDate date;
    private boolean completed;

    public CompletionDto(Long activityId, LocalDate date, boolean completed) {
        this.activityId = activityId;
        this.date = date;
        this.completed = completed;
    }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
