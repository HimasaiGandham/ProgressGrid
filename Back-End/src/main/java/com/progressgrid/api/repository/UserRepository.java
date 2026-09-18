package com.progressgrid.api.repository;

import com.progressgrid.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:q) OR LOWER(u.email) = LOWER(:q) OR LOWER(u.name) = LOWER(:q) OR LOWER(u.email) LIKE LOWER(CONCAT(:q, '@%'))")
    List<User> findMatchingUsers(@Param("q") String query);
}
