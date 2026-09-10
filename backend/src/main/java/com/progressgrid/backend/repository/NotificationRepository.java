package com.progressgrid.backend.repository;

import com.progressgrid.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Messages are deterministic per event, so this keeps re-ticking a box from spamming the feed. */
    boolean existsByUserIdAndMessage(Long userId, String message);
}
