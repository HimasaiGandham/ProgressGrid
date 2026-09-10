package com.progressgrid.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivityDto {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String activityName;

    private String description;

    @Size(max = 20)
    private String frequency;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
