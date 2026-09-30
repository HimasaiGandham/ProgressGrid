package com.progressgrid.api.controller;

import com.progressgrid.api.dto.HabitDTO;
import com.progressgrid.api.dto.ToggleCompletionDTO;
import com.progressgrid.api.security.AuthConfig;
import com.progressgrid.api.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every endpoint acts as the signed-in user from the session token; see {@link AuthConfig}.
 * The browser sends its timezone in X-Timezone so "today" is the user's today, not the server's.
 */
@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private static final String TIMEZONE = "X-Timezone";

    @Autowired
    private HabitService habitService;

    @GetMapping
    public ResponseEntity<List<HabitDTO>> getAllHabits(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                                       @RequestHeader(value = TIMEZONE, required = false) String timeZone) {
        return ResponseEntity.ok(habitService.getAllHabits(userId, HabitService.today(timeZone)));
    }

    @PostMapping
    public ResponseEntity<HabitDTO> createHabit(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                                @RequestHeader(value = TIMEZONE, required = false) String timeZone,
                                                @RequestBody HabitDTO habitDTO) {
        return ResponseEntity.ok(habitService.createHabit(userId, habitDTO, HabitService.today(timeZone)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> toggleCompletion(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                              @PathVariable Long id,
                                              @RequestHeader(value = TIMEZONE, required = false) String timeZone,
                                              @RequestBody ToggleCompletionDTO toggleDTO) {
        habitService.toggleCompletion(userId, id, toggleDTO.getDate(), toggleDTO.isCompleted(),
                HabitService.today(timeZone));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(@RequestAttribute(AuthConfig.USER_ID) Long userId,
                                         @PathVariable Long id) {
        habitService.deleteHabit(userId, id);
        return ResponseEntity.ok().build();
    }
}
