package com.progressgrid.api.service;

import com.progressgrid.api.dto.HabitDTO;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCompletionRepository completionRepository;

    @Autowired
    private HabitCategoryRepository categoryRepository;

    public List<HabitDTO> getAllHabits(Long userId) {
        return habitRepository.findByUserId(userId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public HabitDTO createHabit(Long userId, HabitDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Habit name is required");
        }

        Habit habit = new Habit();
        habit.setUserId(userId);
        habit.setName(dto.getName().trim());
        habit.setDescription(dto.getDescription());
        habit.setFrequency(dto.getFrequency() != null ? dto.getFrequency() : "Daily");
        habit.setTargetDays(dto.getTargetDays() != null ? dto.getTargetDays() : 7);
        habit.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now());

        if (dto.getCategory() != null) {
            habit.setCategory(categoryRepository.findFirstByNameIgnoreCase(dto.getCategory()).orElseGet(() -> {
                HabitCategory category = new HabitCategory();
                category.setName(dto.getCategory());
                category.setColor("#4CAF50");
                return categoryRepository.save(category);
            }));
        }

        return mapToDTO(habitRepository.save(habit));
    }

    public void toggleCompletion(Long userId, Long habitId, LocalDate date, boolean completed) {
        Habit habit = findOwned(userId, habitId);

        // A tick has to fall between the habit's start date and today; ticks outside that range
        // used to be accepted and inflated the completion percentage.
        // ponytail: the server's clock decides "today". Send the client's date if users span timezones.
        if (date == null || date.isBefore(habit.getStartDate()) || date.isAfter(LocalDate.now())) {
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

    private HabitDTO mapToDTO(Habit habit) {
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

        calculateStreaksAndStats(dto, completedDates, habit.getStartDate(), "weekly".equalsIgnoreCase(habit.getFrequency()));
        return dto;
    }

    /**
     * Streaks and completion % in the habit's own unit: days for a daily habit, weeks for a weekly
     * one, where any tick in a Monday-to-Sunday week completes that week.
     */
    private void calculateStreaksAndStats(HabitDTO dto, List<LocalDate> dates, LocalDate startDate, boolean weekly) {
        int step = weekly ? 7 : 1;
        LocalDate now = periodOf(LocalDate.now(), weekly);
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
