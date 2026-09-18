package com.progressgrid.api.repository;

import com.progressgrid.api.model.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {
    List<HabitCompletion> findByHabitId(Long habitId);
    Optional<HabitCompletion> findByHabitIdAndCompletionDate(Long habitId, LocalDate date);
    
    List<HabitCompletion> findByHabitIdAndCompletionDateBetween(Long habitId, LocalDate startDate, LocalDate endDate);
}
