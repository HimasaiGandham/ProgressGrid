package com.progressgrid.backend.controller;

import com.progressgrid.backend.dto.ProgressDto;
import com.progressgrid.backend.repository.ActivityCompletionRepository;
import com.progressgrid.backend.repository.ActivityRepository;
import com.progressgrid.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityCompletionRepository completionRepository;

    @GetMapping("/daily")
    public ResponseEntity<ProgressDto> getDailyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        LocalDate today = LocalDate.now();

        int totalActivities = activityRepository.countByUserId(userId);
        int completedActivities = completionRepository.countByUserIdAndCompletionDateAndCompletedTrue(userId, today);

        ProgressDto progress = new ProgressDto();
        progress.setPlannedActivities(totalActivities);
        progress.setCompletedActivities(completedActivities);
        progress.setDailyPercentage(calculatePercentage(completedActivities, totalActivities));

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/weekly")
    public ResponseEntity<ProgressDto> getWeeklyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int totalActivitiesPerDay = activityRepository.countByUserId(userId);
        
        Map<String, Integer> weeklyData = new LinkedHashMap<>();
        int totalCompletedThisWeek = 0;
        int daysPassed = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            int completedOnDate = completionRepository.countByUserIdAndCompletionDateAndCompletedTrue(userId, date);
            
            int percentage = calculatePercentage(completedOnDate, totalActivitiesPerDay);
            weeklyData.put(date.getDayOfWeek().toString().substring(0, 3), percentage); // Mon, Tue...

            if (!date.isAfter(today)) {
                totalCompletedThisWeek += completedOnDate;
                daysPassed++;
            }
        }

        int plannedSoFar = totalActivitiesPerDay * daysPassed;

        ProgressDto progress = new ProgressDto();
        progress.setWeeklyData(weeklyData);
        progress.setPlannedActivities(plannedSoFar);
        progress.setCompletedActivities(totalCompletedThisWeek);
        progress.setWeeklyAveragePercentage(calculatePercentage(totalCompletedThisWeek, plannedSoFar));

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/monthly")
    public ResponseEntity<ProgressDto> getMonthlyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        int totalActivitiesPerDay = activityRepository.countByUserId(userId);
        int daysPassed = today.getDayOfMonth();
        
        int plannedSoFar = totalActivitiesPerDay * daysPassed;
        int completedSoFar = completionRepository.countCompletedActivitiesInDateRange(userId, startOfMonth, today);

        ProgressDto progress = new ProgressDto();
        progress.setPlannedActivities(plannedSoFar);
        progress.setCompletedActivities(completedSoFar);
        progress.setMonthlyPercentage(calculatePercentage(completedSoFar, plannedSoFar));

        return ResponseEntity.ok(progress);
    }

    private int calculatePercentage(int completed, int total) {
        if (total == 0) return 0;
        return (int) Math.round((double) completed / total * 100);
    }
}
