package com.progressgrid.api.controller;

import com.progressgrid.api.dto.HabitDTO;
import com.progressgrid.api.dto.ToggleCompletionDTO;
import com.progressgrid.api.security.AuthConfig;
import com.progressgrid.api.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Every endpoint acts as the signed-in user from the session token; see {@link AuthConfig}. */
@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*") // Allow frontend to call during dev
public class HabitController {

    @Autowired
    private HabitService habitService;

    @GetMapping
    public ResponseEntity<List<HabitDTO>> getAllHabits(@RequestAttribute(AuthConfig.USER_ID) Long userId) {
        return ResponseEntity.ok(habitService.getAllHabits(userId));
    }

    @PostMapping
    public ResponseEntity<HabitDTO> createHabit(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                                @RequestBody HabitDTO habitDTO) {
        return ResponseEntity.ok(habitService.createHabit(userId, habitDTO));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> toggleCompletion(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                              @PathVariable Long id,
                                              @RequestBody ToggleCompletionDTO toggleDTO) {
        habitService.toggleCompletion(userId, id, toggleDTO.getDate(), toggleDTO.isCompleted());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                         @PathVariable Long id) {
        habitService.deleteHabit(userId, id);
        return ResponseEntity.ok().build();
    }
}
