package com.progressgrid.api.repository;

import com.progressgrid.api.model.HabitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitCategoryRepository extends JpaRepository<HabitCategory, Long> {
}
