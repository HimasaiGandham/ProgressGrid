package com.progressgrid.backend.repository;

import com.progressgrid.backend.model.ActivityCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivityCompletionRepository extends JpaRepository<ActivityCompletion, Long> {

    Optional<ActivityCompletion> findByActivityIdAndCompletionDate(Long activityId, LocalDate completionDate);

    List<ActivityCompletion> findByUserIdAndCompletionDate(Long userId, LocalDate completionDate);

    @Query("SELECT ac FROM ActivityCompletion ac WHERE ac.user.id = :userId AND ac.completionDate >= :startDate AND ac.completionDate <= :endDate")
    List<ActivityCompletion> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    int countByUserIdAndCompletionDateAndCompletedTrue(Long userId, LocalDate completionDate);

    @Query("SELECT COUNT(ac) FROM ActivityCompletion ac WHERE ac.user.id = :userId AND ac.completionDate >= :startDate AND ac.completionDate <= :endDate AND ac.completed = true")
    int countCompletedActivitiesInDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
