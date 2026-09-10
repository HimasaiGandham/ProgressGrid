package com.progressgrid.backend;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API tests over in-memory H2. Each test covers one of the bugs the app shipped
 * with, so a regression fails here rather than silently in the dashboard.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgressGridApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private static final String TODAY = LocalDate.now().toString();

    // --- helpers ---

    /** Registers a fresh user and returns their bearer token. */
    private String signUp() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("name", "Test User", "email", email, "password", "secret123"))))
                .andExpect(status().isOk());

        MvcResult login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", "secret123"))))
                .andExpect(status().isOk())
                .andReturn();

        return "Bearer " + read(login).get("accessToken").asText();
    }

    private long createActivity(String token, String name, String frequency) throws Exception {
        MvcResult result = mvc.perform(post("/api/activities").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("activityName", name, "frequency", frequency))))
                .andExpect(status().isOk())
                .andReturn();
        return read(result).get("id").asLong();
    }

    private void complete(String token, long activityId, String date) throws Exception {
        mvc.perform(post("/api/activities/" + activityId + "/complete")
                        .param("date", date).header("Authorization", token))
                .andExpect(status().isOk());
    }

    private JsonNode progress(String token, String window) throws Exception {
        return read(mvc.perform(get("/api/progress/" + window).header("Authorization", token))
                .andExpect(status().isOk()).andReturn());
    }

    private JsonNode read(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }

    // --- tests ---

    @Test
    void protectedEndpointsRejectAnonymousCallers() throws Exception {
        mvc.perform(get("/api/activities")).andExpect(status().isUnauthorized());
    }

    @Test
    void registerThenLoginIssuesAWorkingToken() throws Exception {
        String token = signUp();

        mvc.perform(get("/api/activities").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void loginRejectsAWrongPassword() throws Exception {
        signUp();
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("email", "nobody@example.com", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    /** The weekly grid used to render blank because nothing exposed stored ticks. */
    @Test
    void completionsEndpointReturnsTicksAlreadyStored() throws Exception {
        String token = signUp();
        long id = createActivity(token, "Read", "DAILY");
        complete(token, id, TODAY);

        mvc.perform(get("/api/activities/completions")
                        .param("start", TODAY).param("end", TODAY)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityId").value(id))
                .andExpect(jsonPath("$[0].date").value(TODAY))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void unCompletingClearsTheTick() throws Exception {
        String token = signUp();
        long id = createActivity(token, "Read", "DAILY");
        complete(token, id, TODAY);

        mvc.perform(post("/api/activities/" + id + "/uncomplete")
                        .param("date", TODAY).header("Authorization", token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/activities/completions")
                        .param("start", TODAY).param("end", TODAY)
                        .header("Authorization", token))
                .andExpect(jsonPath("$[0].completed").value(false));

        assertThat(progress(token, "daily").get("dailyPercentage").asInt()).isZero();
    }

    @Test
    void dailyProgressReachesFullMarksWhenEveryDailyHabitIsDone() throws Exception {
        String token = signUp();
        complete(token, createActivity(token, "Read", "DAILY"), TODAY);
        complete(token, createActivity(token, "Walk", "DAILY"), TODAY);

        JsonNode daily = progress(token, "daily");
        assertThat(daily.get("plannedActivities").asInt()).isEqualTo(2);
        assertThat(daily.get("completedActivities").asInt()).isEqualTo(2);
        assertThat(daily.get("dailyPercentage").asInt()).isEqualTo(100);
    }

    /** A weekly habit is not due every day; it used to be counted as planned all seven. */
    @Test
    void weeklyHabitsAreNotCountedAgainstTheDailyTarget() throws Exception {
        String token = signUp();
        long daily = createActivity(token, "Read", "DAILY");
        createActivity(token, "Deep clean", "WEEKLY");
        complete(token, daily, TODAY);

        JsonNode day = progress(token, "daily");
        assertThat(day.get("plannedActivities").asInt()).isEqualTo(1);
        assertThat(day.get("dailyPercentage").asInt()).isEqualTo(100);

        // The weekly one is still expected once across the week, so the week is not yet complete.
        JsonNode week = progress(token, "weekly");
        assertThat(week.get("plannedActivities").asInt()).isGreaterThan(1);
        assertThat(week.get("weeklyAveragePercentage").asInt()).isLessThan(100);
        assertThat(week.get("weeklyData").get(
                LocalDate.now().getDayOfWeek().toString().substring(0, 3)).asInt()).isEqualTo(100);
    }

    @Test
    void progressNeverExceedsOneHundredPercent() throws Exception {
        String token = signUp();
        long weekly = createActivity(token, "Deep clean", "WEEKLY");
        complete(token, weekly, TODAY);
        complete(token, weekly, LocalDate.now().minusDays(1).toString());

        assertThat(progress(token, "monthly").get("monthlyPercentage").asInt()).isLessThanOrEqualTo(100);
    }

    /** Nothing used to write notifications, so the feed was permanently empty. */
    @Test
    void finishingEveryDailyHabitWritesExactlyOneNotification() throws Exception {
        String token = signUp();
        long id = createActivity(token, "Read", "DAILY");

        complete(token, id, TODAY);
        complete(token, id, TODAY); // re-ticking must not duplicate the message

        mvc.perform(get("/api/notifications").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notificationType").value("DAY_COMPLETE"))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void notificationsCanBeMarkedRead() throws Exception {
        String token = signUp();
        complete(token, createActivity(token, "Read", "DAILY"), TODAY);

        MvcResult listed = mvc.perform(get("/api/notifications").header("Authorization", token)).andReturn();
        long notificationId = read(listed).get(0).get("id").asLong();

        mvc.perform(put("/api/notifications/" + notificationId + "/read").header("Authorization", token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/notifications").header("Authorization", token))
                .andExpect(jsonPath("$[0].isRead").value(true));
    }

    @Test
    void activitiesCanBeRenamedAndRecadenced() throws Exception {
        String token = signUp();
        long id = createActivity(token, "Read", "DAILY");

        mvc.perform(put("/api/activities/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "activityName", "Read 20 pages", "frequency", "WEEKLY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityName").value("Read 20 pages"))
                .andExpect(jsonPath("$.frequency").value("WEEKLY"));
    }

    @Test
    void blankActivityNamesAreRejected() throws Exception {
        String token = signUp();
        mvc.perform(post("/api/activities").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("activityName", "  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oneUserCannotReadOrChangeAnothersActivity() throws Exception {
        String alice = signUp();
        String mallory = signUp();
        long aliceActivity = createActivity(alice, "Read", "DAILY");

        mvc.perform(get("/api/activities").header("Authorization", mallory))
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(put("/api/activities/" + aliceActivity).header("Authorization", mallory)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("activityName", "Hijacked"))))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/activities/" + aliceActivity + "/complete")
                        .param("date", TODAY).header("Authorization", mallory))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/activities/" + aliceActivity).header("Authorization", mallory))
                .andExpect(status().isNotFound());
    }
}
