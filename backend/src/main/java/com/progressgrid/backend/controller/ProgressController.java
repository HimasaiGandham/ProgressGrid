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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Percentages are completed / planned, where "planned" respects each activity's cadence:
 * a DAILY activity is expected once per day, a WEEKLY one once per week. Counting every
 * activity as due every day is what used to make weekly habits drag the score down.
 */
@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private static final String DAILY = "DAILY";
    private static final String WEEKLY = "WEEKLY";

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityCompletionRepository completionRepository;

    @GetMapping("/daily")
    public ResponseEntity<ProgressDto> getDailyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        LocalDate today = LocalDate.now();

        // A day's score is about daily habits only; weekly ones are not due on any particular day.
        int planned = activityRepository.countByUserIdAndFrequency(userId, DAILY);
        int completed = completionRepository.countCompletedOnDateByFrequency(userId, today, DAILY);

        ProgressDto progress = new ProgressDto();
        progress.setPlannedActivities(planned);
        progress.setCompletedActivities(completed);
        progress.setDailyPercentage(calculatePercentage(completed, planned));

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/weekly")
    public ResponseEntity<ProgressDto> getWeeklyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int dailyQuota = activityRepository.countByUserIdAndFrequency(userId, DAILY);
        int weeklyQuota = activityRepository.countByUserIdAndFrequency(userId, WEEKLY);

        // One bar per weekday, each showing that day's daily-habit completion rate.
        Map<String, Integer> weeklyData = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            int completedOnDate = completionRepository.countCompletedOnDateByFrequency(userId, date, DAILY);
            weeklyData.put(date.getDayOfWeek().toString().substring(0, 3), // MON, TUE...
                    calculatePercentage(completedOnDate, dailyQuota));
        }

        int daysElapsed = (int) ChronoUnit.DAYS.between(startOfWeek, today) + 1;
        int planned = dailyQuota * daysElapsed + weeklyQuota;
        int completed = completionRepository.countCompletedInDateRange(userId, startOfWeek, today);

        ProgressDto progress = new ProgressDto();
        progress.setWeeklyData(weeklyData);
        progress.setPlannedActivities(planned);
        progress.setCompletedActivities(completed);
        progress.setWeeklyAveragePercentage(calculatePercentage(completed, planned));

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/monthly")
    public ResponseEntity<ProgressDto> getMonthlyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        int dailyQuota = activityRepository.countByUserIdAndFrequency(userId, DAILY);
        int weeklyQuota = activityRepository.countByUserIdAndFrequency(userId, WEEKLY);

        int daysElapsed = today.getDayOfMonth();
        int weeksElapsed = (daysElapsed + 6) / 7; // part-weeks still count as one

        int planned = dailyQuota * daysElapsed + weeklyQuota * weeksElapsed;
        int completed = completionRepository.countCompletedInDateRange(userId, startOfMonth, today);

        ProgressDto progress = new ProgressDto();
        progress.setPlannedActivities(planned);
        progress.setCompletedActivities(completed);
        progress.setMonthlyPercentage(calculatePercentage(completed, planned));

        return ResponseEntity.ok(progress);
    }

    /** Capped at 100: a weekly habit ticked on several days would otherwise read over target. */
    private int calculatePercentage(int completed, int total) {
        if (total <= 0) return 0;
        return Math.min(100, (int) Math.round((double) completed / total * 100));
    }
}
