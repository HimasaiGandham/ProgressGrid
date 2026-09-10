package com.progressgrid.backend.controller;

import com.progressgrid.backend.dto.ActivityDto;
import com.progressgrid.backend.dto.CompletionDto;
import com.progressgrid.backend.model.Activity;
import com.progressgrid.backend.model.ActivityCompletion;
import com.progressgrid.backend.model.Notification;
import com.progressgrid.backend.model.User;
import com.progressgrid.backend.repository.ActivityCompletionRepository;
import com.progressgrid.backend.repository.ActivityRepository;
import com.progressgrid.backend.repository.NotificationRepository;
import com.progressgrid.backend.repository.UserRepository;
import com.progressgrid.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private static final String DAILY = "DAILY";

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityCompletionRepository completionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<ActivityDto> getActivities(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return activityRepository.findByUserId(currentUser.getId()).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Completion state for a date range, so the weekly grid can render the ticks that are
     * already stored instead of starting blank on every page load.
     */
    @GetMapping("/completions")
    public List<CompletionDto> getCompletions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return completionRepository.findByUserIdAndDateRange(currentUser.getId(), start, end).stream()
                .map(c -> new CompletionDto(c.getActivity().getId(), c.getCompletionDate(),
                        Boolean.TRUE.equals(c.getCompleted())))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ActivityDto createActivity(@Valid @RequestBody ActivityDto activityDto,
                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        Activity activity = new Activity();
        activity.setUser(user);
        applyDto(activity, activityDto);
        return convertToDto(activityRepository.save(activity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityDto> updateActivity(@PathVariable Long id,
                                                      @Valid @RequestBody ActivityDto activityDto,
                                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<Activity> activityOpt = findOwned(id, currentUser);
        if (activityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Activity activity = activityOpt.get();
        applyDto(activity, activityDto);
        return ResponseEntity.ok(convertToDto(activityRepository.save(activity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (findOwned(id, currentUser).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        activityRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeActivity(@PathVariable Long id,
                                              @RequestParam(required = false) String date,
                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        return handleCompletion(id, date, currentUser, true);
    }

    @PostMapping("/{id}/uncomplete")
    public ResponseEntity<?> uncompleteActivity(@PathVariable Long id,
                                                @RequestParam(required = false) String date,
                                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        return handleCompletion(id, date, currentUser, false);
    }

    private ResponseEntity<?> handleCompletion(Long id, String dateStr, CustomUserDetails currentUser,
                                               boolean isComplete) {
        Optional<Activity> activityOpt = findOwned(id, currentUser);
        if (activityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();

        ActivityCompletion completion = completionRepository
                .findByActivityIdAndUserIdAndCompletionDate(id, currentUser.getId(), date)
                .orElseGet(() -> {
                    ActivityCompletion fresh = new ActivityCompletion();
                    fresh.setActivity(activityOpt.get());
                    fresh.setUser(activityOpt.get().getUser());
                    fresh.setCompletionDate(date);
                    return fresh;
                });
        completion.setCompleted(isComplete);
        completionRepository.save(completion);

        if (isComplete) {
            notifyIfDayFinished(currentUser.getId(), date);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * The only thing that writes notifications: clearing every daily activity for a date.
     * ponytail: one trigger is enough to make the feed real - add streaks/reminders when asked.
     */
    private void notifyIfDayFinished(Long userId, LocalDate date) {
        int quota = activityRepository.countByUserIdAndFrequency(userId, DAILY);
        if (quota == 0 || completionRepository.countCompletedOnDateByFrequency(userId, date, DAILY) != quota) {
            return;
        }

        String message = "All " + quota + " daily activities done on " + date + ". Nice work!";
        if (notificationRepository.existsByUserIdAndMessage(userId, message)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(userRepository.findById(userId).orElseThrow());
        notification.setMessage(message);
        notification.setNotificationType("DAY_COMPLETE");
        notificationRepository.save(notification);
    }

    private Optional<Activity> findOwned(Long id, CustomUserDetails currentUser) {
        return activityRepository.findById(id)
                .filter(a -> a.getUser().getId().equals(currentUser.getId()));
    }

    private void applyDto(Activity activity, ActivityDto dto) {
        activity.setActivityName(dto.getActivityName());
        activity.setDescription(dto.getDescription());
        if (dto.getFrequency() != null) {
            activity.setFrequency(dto.getFrequency());
        }
    }

    private ActivityDto convertToDto(Activity activity) {
        ActivityDto dto = new ActivityDto();
        dto.setId(activity.getId());
        dto.setActivityName(activity.getActivityName());
        dto.setDescription(activity.getDescription());
        dto.setFrequency(activity.getFrequency());
        return dto;
    }
}
