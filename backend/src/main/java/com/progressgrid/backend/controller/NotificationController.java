package com.progressgrid.backend.controller;

import com.progressgrid.backend.model.Notification;
import com.progressgrid.backend.repository.NotificationRepository;
import com.progressgrid.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> getNotifications(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isPresent() && notifOpt.get().getUser().getId().equals(currentUser.getId())) {
            Notification notification = notifOpt.get();
            notification.setIsRead(true);
            notificationRepository.save(notification);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
