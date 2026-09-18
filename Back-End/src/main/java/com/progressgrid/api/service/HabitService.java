package com.progressgrid.api.service;

import com.progressgrid.api.dto.HabitDTO;
import com.progressgrid.api.model.Habit;
import com.progressgrid.api.model.HabitCategory;
import com.progressgrid.api.model.HabitCompletion;
import com.progressgrid.api.model.User;
import com.progressgrid.api.repository.HabitCategoryRepository;
import com.progressgrid.api.repository.HabitCompletionRepository;
import com.progressgrid.api.repository.HabitRepository;
import com.progressgrid.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCompletionRepository completionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitCategoryRepository categoryRepository;

    public List<HabitDTO> getAllHabits(Long userId) {
        List<Habit> habits = habitRepository.findByUserId(userId);
        return habits.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public HabitDTO createHabit(Long userId, HabitDTO dto) {
        Habit habit = new Habit();
        habit.setUserId(userId);
        habit.setName(dto.getName());
        habit.setDescription(dto.getDescription());
        habit.setFrequency(dto.getFrequency() != null ? dto.getFrequency() : "Daily");
        habit.setTargetDays(dto.getTargetDays() != null ? dto.getTargetDays() : 7);
        habit.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now());

        if (dto.getCategory() != null) {
            // Find or create category
            List<HabitCategory> cats = categoryRepository.findAll();
            HabitCategory cat = cats.stream().filter(c -> c.getName().equalsIgnoreCase(dto.getCategory())).findFirst().orElseGet(() -> {
                HabitCategory newCat = new HabitCategory();
                newCat.setName(dto.getCategory());
                newCat.setColor("#4CAF50");
                return categoryRepository.save(newCat);
            });
            habit.setCategory(cat);
        }

        Habit saved = habitRepository.save(habit);
        return mapToDTO(saved);
    }

    public void toggleCompletion(Long habitId, LocalDate date, boolean completed) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new RuntimeException("Habit not found"));
        
        Optional<HabitCompletion> existing = completionRepository.findByHabitIdAndCompletionDate(habitId, date);
        if (existing.isPresent()) {
            if (!completed) {
                completionRepository.delete(existing.get());
            } else {
                HabitCompletion comp = existing.get();
                comp.setCompleted(true);
                completionRepository.save(comp);
            }
        } else if (completed) {
            HabitCompletion comp = new HabitCompletion();
            comp.setHabit(habit);
            comp.setCompletionDate(date);
            comp.setCompleted(true);
            completionRepository.save(comp);
        }
    }

    public void deleteHabit(Long id) {
        habitRepository.deleteById(id);
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

        List<HabitCompletion> comps = completionRepository.findByHabitId(habit.getId());
        List<LocalDate> completedDates = comps.stream()
                .filter(HabitCompletion::getCompleted)
                .map(HabitCompletion::getCompletionDate)
                .sorted()
                .collect(Collectors.toList());
        dto.setCompletions(completedDates);

        // Calculate streaks
        calculateStreaksAndStats(dto, completedDates, habit.getStartDate());

        return dto;
    }

    private void calculateStreaksAndStats(HabitDTO dto, List<LocalDate> dates, LocalDate startDate) {
        if (dates.isEmpty()) {
            dto.setCurrentStreak(0);
            dto.setBestStreak(0);
            dto.setCompletedDays(0);
            dto.setCompletionPercentage(0);
            return;
        }

        int currentStreak = 0;
        int bestStreak = 0;
        int tempStreak = 0;

        LocalDate today = LocalDate.now();
        LocalDate lastDate = null;

        // Simple contiguous days calculation
        for (LocalDate date : dates) {
            if (lastDate == null) {
                tempStreak = 1;
            } else if (date.equals(lastDate.plusDays(1))) {
                tempStreak++;
            } else if (!date.equals(lastDate)) { // ignores duplicates if any
                tempStreak = 1;
            }
            if (tempStreak > bestStreak) bestStreak = tempStreak;
            lastDate = date;
        }

        // Calculate current streak
        if (dates.contains(today) || dates.contains(today.minusDays(1))) {
            tempStreak = 0;
            LocalDate checkDate = dates.contains(today) ? today : today.minusDays(1);
            while (dates.contains(checkDate)) {
                tempStreak++;
                checkDate = checkDate.minusDays(1);
            }
            currentStreak = tempStreak;
        }

        dto.setCurrentStreak(currentStreak);
        dto.setBestStreak(bestStreak);
        dto.setCompletedDays(dates.size());
        
        long totalDaysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, today) + 1;
        if (totalDaysSinceStart <= 0) totalDaysSinceStart = 1; // Prevent div by zero
        int percentage = (int) Math.round(((double) dates.size() / totalDaysSinceStart) * 100);
        if(percentage > 100) percentage = 100;
        dto.setCompletionPercentage(percentage);
    }
}
