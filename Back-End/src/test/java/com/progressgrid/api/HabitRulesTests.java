package com.progressgrid.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The habit rules the dashboard relies on: valid input, which days can be ticked, and scoring. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HabitRulesTests {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String signUp() throws Exception {
        String name = "h" + UUID.randomUUID().toString().substring(0, 8);
        String body = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", name, "email", name + "@example.com", "password", "secret123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    private ResultActions createHabit(String token, Map<String, Object> habit) throws Exception {
        return mvc.perform(post("/api/habits").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(habit)));
    }

    private long habitStarting(String token, LocalDate start, String frequency) throws Exception {
        String body = createHabit(token, Map.of("name", "Read", "frequency", frequency, "startDate", start.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    private ResultActions tick(String token, long habitId, LocalDate date) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("completed", true);
        if (date != null) {
            body.put("date", date.toString());
        }
        return mvc.perform(post("/api/habits/" + habitId + "/complete").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private JsonNode habit(String token, long habitId) throws Exception {
        String body = mvc.perform(get("/api/habits").header("Authorization", token)).andReturn().getResponse().getContentAsString();
        for (JsonNode h : json.readTree(body)) {
            if (h.get("id").asLong() == habitId) {
                return h;
            }
        }
        throw new AssertionError("habit " + habitId + " not returned");
    }

    @Test
    void aHabitNeedsAName() throws Exception {
        String token = signUp();
        createHabit(token, Map.of("category", "Health")).andExpect(status().isBadRequest());
        createHabit(token, Map.of("name", "   ")).andExpect(status().isBadRequest());
    }

    @Test
    void onlyDaysFromTheStartDateUpToTodayCanBeTicked() throws Exception {
        String token = signUp();
        long id = habitStarting(token, TODAY.minusDays(3), "Daily");

        tick(token, id, TODAY.minusDays(4)).andExpect(status().isBadRequest());
        tick(token, id, TODAY.plusDays(1)).andExpect(status().isBadRequest());
        tick(token, id, null).andExpect(status().isBadRequest());
        tick(token, id, TODAY.minusDays(3)).andExpect(status().isOk());
        tick(token, id, TODAY).andExpect(status().isOk());
    }

    @Test
    void dailyStreaksAndCompletion() throws Exception {
        String token = signUp();
        long id = habitStarting(token, TODAY.minusDays(9), "Daily");

        tick(token, id, TODAY.minusDays(1));
        tick(token, id, TODAY.minusDays(2));
        JsonNode h = habit(token, id);
        // Today isn't ticked yet, so the run ending yesterday is still the current streak.
        assertThat(h.get("currentStreak").asInt()).isEqualTo(2);

        tick(token, id, TODAY.minusDays(5));
        tick(token, id, TODAY.minusDays(6));
        tick(token, id, TODAY);
        h = habit(token, id);
        assertThat(h.get("currentStreak").asInt()).isEqualTo(3);
        assertThat(h.get("bestStreak").asInt()).isEqualTo(3);
        assertThat(h.get("completionPercentage").asInt()).isEqualTo(50); // 5 of 10 days
    }

    @Test
    void weeklyHabitsAreScoredInWeeks() throws Exception {
        String token = signUp();
        LocalDate lastMonday = TODAY.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        long id = habitStarting(token, lastMonday, "Weekly");

        tick(token, id, lastMonday);
        tick(token, id, TODAY);
        tick(token, id, TODAY.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))); // twice in one week counts once
        JsonNode h = habit(token, id);
        assertThat(h.get("completionPercentage").asInt()).isEqualTo(100); // 2 of 2 weeks
        assertThat(h.get("currentStreak").asInt()).isEqualTo(2);
    }
}
