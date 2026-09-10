package com.progressgrid.backend.repository;

import com.progressgrid.backend.model.ActivityCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivityCompletionRepository extends JpaRepository<ActivityCompletion, Long> {

    /** Scoped by user as well as activity so a stray id can never reach another account's row. */
    Optional<ActivityCompletion> findByActivityIdAndUserIdAndCompletionDate(
            Long activityId, Long userId, LocalDate completionDate);

    @Query("SELECT ac FROM ActivityCompletion ac WHERE ac.user.id = :userId "
            + "AND ac.completionDate BETWEEN :startDate AND :endDate")
    List<ActivityCompletion> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ac) FROM ActivityCompletion ac WHERE ac.user.id = :userId "
            + "AND ac.completionDate = :date AND ac.completed = true "
            + "AND ac.activity.frequency = :frequency")
    int countCompletedOnDateByFrequency(@Param("userId") Long userId,
                                        @Param("date") LocalDate date,
                                        @Param("frequency") String frequency);

    @Query("SELECT COUNT(ac) FROM ActivityCompletion ac WHERE ac.user.id = :userId "
            + "AND ac.completionDate BETWEEN :startDate AND :endDate AND ac.completed = true")
    int countCompletedInDateRange(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
}
