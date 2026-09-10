package com.progressgrid.backend.repository;

import com.progressgrid.backend.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserId(Long userId);

    /** How many activities the user expects to do at a given cadence ("DAILY" / "WEEKLY"). */
    int countByUserIdAndFrequency(Long userId, String frequency);
}
