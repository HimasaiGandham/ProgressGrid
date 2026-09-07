package com.progressgrid.api.controller;

import com.progressgrid.api.dto.HabitDTO;
import com.progressgrid.api.dto.ToggleCompletionDTO;
import com.progressgrid.api.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*") // Allow frontend to call during dev
public class HabitController {

    @Autowired
    private HabitService habitService;

    @GetMapping
    public ResponseEntity<List<HabitDTO>> getAllHabits() {
        return ResponseEntity.ok(habitService.getAllHabits());
    }

    @PostMapping
    public ResponseEntity<HabitDTO> createHabit(@RequestBody HabitDTO habitDTO) {
        return ResponseEntity.ok(habitService.createHabit(habitDTO));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> toggleCompletion(@PathVariable Long id, @RequestBody ToggleCompletionDTO toggleDTO) {
        habitService.toggleCompletion(id, toggleDTO.getDate(), toggleDTO.isCompleted());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.ok().build();
    }
}
