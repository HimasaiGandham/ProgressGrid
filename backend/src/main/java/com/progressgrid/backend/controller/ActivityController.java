package com.progressgrid.backend.controller;

import com.progressgrid.backend.dto.ActivityDto;
import com.progressgrid.backend.model.Activity;
import com.progressgrid.backend.model.ActivityCompletion;
import com.progressgrid.backend.model.User;
import com.progressgrid.backend.repository.ActivityCompletionRepository;
import com.progressgrid.backend.repository.ActivityRepository;
import com.progressgrid.backend.repository.UserRepository;
import com.progressgrid.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityCompletionRepository completionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<ActivityDto> getActivities(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return activityRepository.findByUserId(currentUser.getId()).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @PostMapping
    public ActivityDto createActivity(@Valid @RequestBody ActivityDto activityDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setActivityName(activityDto.getActivityName());
        activity.setDescription(activityDto.getDescription());
        if(activityDto.getFrequency() != null) {
            activity.setFrequency(activityDto.getFrequency());
        }

        Activity savedActivity = activityRepository.save(activity);
        return convertToDto(savedActivity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityDto> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityDto activityDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<Activity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isPresent() && activityOpt.get().getUser().getId().equals(currentUser.getId())) {
            Activity activity = activityOpt.get();
            activity.setActivityName(activityDto.getActivityName());
            activity.setDescription(activityDto.getDescription());
            if(activityDto.getFrequency() != null) {
                activity.setFrequency(activityDto.getFrequency());
            }
            Activity updated = activityRepository.save(activity);
            return ResponseEntity.ok(convertToDto(updated));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<Activity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isPresent() && activityOpt.get().getUser().getId().equals(currentUser.getId())) {
            activityRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeActivity(@PathVariable Long id, @RequestParam(required = false) String date, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return handleCompletion(id, date, currentUser, true);
    }

    @PostMapping("/{id}/uncomplete")
    public ResponseEntity<?> uncompleteActivity(@PathVariable Long id, @RequestParam(required = false) String date, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return handleCompletion(id, date, currentUser, false);
    }

    private ResponseEntity<?> handleCompletion(Long id, String dateStr, CustomUserDetails currentUser, boolean isComplete) {
        Optional<Activity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isPresent() && activityOpt.get().getUser().getId().equals(currentUser.getId())) {
            LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();

            Optional<ActivityCompletion> completionOpt = completionRepository.findByActivityIdAndCompletionDate(id, date);
            
            ActivityCompletion completion;
            if (completionOpt.isPresent()) {
                completion = completionOpt.get();
                completion.setCompleted(isComplete);
            } else {
                completion = new ActivityCompletion();
                completion.setActivity(activityOpt.get());
                completion.setUser(activityOpt.get().getUser());
                completion.setCompletionDate(date);
                completion.setCompleted(isComplete);
            }
            completionRepository.save(completion);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
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
