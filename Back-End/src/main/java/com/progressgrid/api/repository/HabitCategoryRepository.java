package com.progressgrid.api.repository;

import com.progressgrid.api.model.HabitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HabitCategoryRepository extends JpaRepository<HabitCategory, Long> {
    Optional<HabitCategory> findFirstByNameIgnoreCase(String name);
}
