package com.progressgrid.api.service;

import com.progressgrid.api.dto.HabitDTO;
import com.progressgrid.api.dto.HabitStatsDTO;
import com.progressgrid.api.model.Habit;
import com.progressgrid.api.model.HabitCategory;
import com.progressgrid.api.model.HabitCompletion;
import com.progressgrid.api.repository.HabitCategoryRepository;
import com.progressgrid.api.repository.HabitCompletionRepository;
import com.progressgrid.api.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCompletionRepository completionRepository;

    @Autowired
    private HabitCategoryRepository categoryRepository;

    /**
     * "Today" for the caller: the date in their timezone (an IANA id such as "Asia/Kolkata", sent
     * by the browser), or the server's date if none or an unknown one is given. Using the server's
     * clock rejected ticks from anyone already past midnight while the server was still on the
     * previous day.
     */
    public static LocalDate today(String timeZone) {
        if (timeZone != null && !timeZone.isBlank()) {
            try {
                return LocalDate.now(ZoneId.of(timeZone.trim()));
            } catch (DateTimeException e) {
                // Unknown zone: fall through to the server's date.
            }
        }
        return LocalDate.now();
    }

    public List<HabitDTO> getAllHabits(Long userId, LocalDate today) {
        return habitRepository.findByUserId(userId).stream().map(habit -> mapToDTO(habit, today)).collect(Collectors.toList());
    }

    public HabitStatsDTO getHabitStats(Long userId, LocalDate today) {
        List<HabitDTO> habits = getAllHabits(userId, today);
        HabitStatsDTO stats = new HabitStatsDTO();
        stats.setTotalHabits(habits.size());

        if (habits.isEmpty()) {
            stats.setCategoryCounts(Collections.emptyMap());
            stats.setCategoryCompletionRates(Collections.emptyMap());
            stats.setDayOfWeekCompletionRates(Collections.emptyMap());
            stats.setTopHabits(Collections.emptyList());
            return stats;
        }

        int totalTicks = 0;
        int bestStreak = 0;
        int currentStreak = 0;
        int sumCompletion = 0;

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Integer> categorySums = new HashMap<>();
        Map<DayOfWeek, Integer> dayOfWeekTicks = new HashMap<>();

        for (HabitDTO h : habits) {
            List<LocalDate> comp = h.getCompletions();
            int compSize = comp != null ? comp.size() : 0;
            totalTicks += compSize;

            if (h.getBestStreak() != null && h.getBestStreak() > bestStreak) {
                bestStreak = h.getBestStreak();
            }
            if (h.getCurrentStreak() != null && h.getCurrentStreak() > currentStreak) {
                currentStreak = h.getCurrentStreak();
            }
            int pct = h.getCompletionPercentage() != null ? h.getCompletionPercentage() : 0;
            sumCompletion += pct;

            String cat = (h.getCategory() != null && !h.getCategory().isBlank()) ? h.getCategory() : "General";
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            categorySums.put(cat, categorySums.getOrDefault(cat, 0) + pct);

            if (comp != null) {
                for (LocalDate d : comp) {
                    dayOfWeekTicks.put(d.getDayOfWeek(), dayOfWeekTicks.getOrDefault(d.getDayOfWeek(), 0) + 1);
                }
            }
        }

        stats.setTotalCompletions(totalTicks);
        stats.setBestStreak(bestStreak);
        stats.setCurrentStreak(currentStreak);
        stats.setOverallCompletionPercentage((int) Math.round((double) sumCompletion / habits.size()));

        Map<String, Integer> categoryRates = new HashMap<>();
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            int count = entry.getValue();
            int totalPct = categorySums.getOrDefault(entry.getKey(), 0);
            categoryRates.put(entry.getKey(), (int) Math.round((double) totalPct / count));
        }

        Map<String, Integer> dayOfWeekRates = new LinkedHashMap<>();
        DayOfWeek[] daysOrder = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };
        for (DayOfWeek dow : daysOrder) {
            dayOfWeekRates.put(dow.name().substring(0, 3), dayOfWeekTicks.getOrDefault(dow, 0));
        }

        // Count perfect days in the last 30 days
        int perfectDaysCount = 0;
        List<HabitDTO> dailyHabits = habits.stream()
                .filter(h -> !"weekly".equalsIgnoreCase(h.getFrequency()))
                .collect(Collectors.toList());

        if (!dailyHabits.isEmpty()) {
            for (int i = 0; i < 30; i++) {
                LocalDate d = today.minusDays(i);
                List<HabitDTO> activeOnDay = dailyHabits.stream()
                        .filter(h -> h.getStartDate() != null && !d.isBefore(h.getStartDate()))
                        .collect(Collectors.toList());
                if (!activeOnDay.isEmpty()) {
                    boolean allDone = activeOnDay.stream()
                            .allMatch(h -> h.getCompletions() != null && h.getCompletions().contains(d));
                    if (allDone) {
                        perfectDaysCount++;
                    }
                }
            }
        }
        stats.setPerfectDays(perfectDaysCount);

        List<HabitDTO> topHabits = habits.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getCompletionPercentage() != null ? b.getCompletionPercentage() : 0,
                        a.getCompletionPercentage() != null ? a.getCompletionPercentage() : 0))
                .limit(5)
                .collect(Collectors.toList());

        stats.setCategoryCounts(categoryCounts);
        stats.setCategoryCompletionRates(categoryRates);
        stats.setDayOfWeekCompletionRates(dayOfWeekRates);
        stats.setTopHabits(topHabits);

        return stats;
    }

    public HabitDTO createHabit(Long userId, HabitDTO dto, LocalDate today) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Habit name is required");
        }

        Habit habit = new Habit();
        habit.setUserId(userId);
        habit.setName(dto.getName().trim());
        habit.setDescription(dto.getDescription());
        habit.setFrequency(dto.getFrequency() != null ? dto.getFrequency() : "Daily");
        habit.setTargetDays(dto.getTargetDays() != null ? dto.getTargetDays() : 7);
        habit.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : today);

        if (dto.getCategory() != null) {
            habit.setCategory(categoryRepository.findFirstByNameIgnoreCase(dto.getCategory()).orElseGet(() -> {
                HabitCategory category = new HabitCategory();
                category.setName(dto.getCategory());
                category.setColor("#4CAF50");
                return categoryRepository.save(category);
            }));
        }

        return mapToDTO(habitRepository.save(habit), today);
    }

    public void toggleCompletion(Long userId, Long habitId, LocalDate date, boolean completed, LocalDate today) {
        Habit habit = findOwned(userId, habitId);

        // A tick has to fall between the habit's start date and the caller's today; ticks outside
        // that range used to be accepted and inflated the completion percentage.
        if (date == null || date.isBefore(habit.getStartDate()) || date.isAfter(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must be between the habit's start date and today");
        }

        Optional<HabitCompletion> existing = completionRepository.findByHabitIdAndCompletionDate(habitId, date);
        if (!completed) {
            existing.ifPresent(completionRepository::delete);
            return;
        }
        HabitCompletion completion = existing.orElseGet(HabitCompletion::new);
        completion.setHabit(habit);
        completion.setCompletionDate(date);
        completion.setCompleted(true);
        completionRepository.save(completion);
    }

    public void deleteHabit(Long userId, Long id) {
        habitRepository.delete(findOwned(userId, id));
    }

    /**
     * The habit, if it belongs to this user. Someone else's habit is reported as not found
     * rather than forbidden, so habit ids can't be probed for existence.
     */
    private Habit findOwned(Long userId, Long habitId) {
        return habitRepository.findById(habitId)
                .filter(habit -> userId.equals(habit.getUserId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habit not found"));
    }

    private HabitDTO mapToDTO(Habit habit, LocalDate today) {
        HabitDTO dto = new HabitDTO();
        dto.setId(habit.getId());
        dto.setName(habit.getName());
        dto.setDescription(habit.getDescription());
        dto.setCategory(habit.getCategory() != null ? habit.getCategory().getName() : null);
        dto.setFrequency(habit.getFrequency());
        dto.setTargetDays(habit.getTargetDays());
        dto.setStartDate(habit.getStartDate());

        List<LocalDate> completedDates = completionRepository.findByHabitId(habit.getId()).stream()
                .filter(HabitCompletion::getCompleted)
                .map(HabitCompletion::getCompletionDate)
                .sorted()
                .collect(Collectors.toList());
        dto.setCompletions(completedDates);

        calculateStreaksAndStats(dto, completedDates, habit.getStartDate(), today, "weekly".equalsIgnoreCase(habit.getFrequency()));
        return dto;
    }

    /**
     * Streaks and completion % in the habit's own unit: days for a daily habit, weeks for a weekly
     * one, where any tick in a Monday-to-Sunday week completes that week.
     */
    private void calculateStreaksAndStats(HabitDTO dto, List<LocalDate> dates, LocalDate startDate, LocalDate today, boolean weekly) {
        int step = weekly ? 7 : 1;
        LocalDate now = periodOf(today, weekly);
        LocalDate first = periodOf(startDate, weekly);
        TreeSet<LocalDate> done = dates.stream()
                .map(date -> periodOf(date, weekly))
                .filter(period -> !period.isBefore(first) && !period.isAfter(now))
                .collect(Collectors.toCollection(TreeSet::new));

        int best = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate period : done) {
            run = period.minusDays(step).equals(previous) ? run + 1 : 1;
            best = Math.max(best, run);
            previous = period;
        }

        // Today (or this week) may simply not be done yet, so a run ending one period back still counts.
        int current = 0;
        for (LocalDate p = done.contains(now) ? now : now.minusDays(step); done.contains(p); p = p.minusDays(step)) {
            current++;
        }

        long periods = Math.max(1, ChronoUnit.DAYS.between(first, now) / step + 1);
        dto.setCurrentStreak(current);
        dto.setBestStreak(best);
        dto.setCompletedDays(dates.size());
        dto.setCompletionPercentage((int) Math.min(100, Math.round(100.0 * done.size() / periods)));
    }

    private static LocalDate periodOf(LocalDate date, boolean weekly) {
        return weekly ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : date;
    }
}
