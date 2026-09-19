package com.progressgrid.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progressgrid.api.model.User;
import com.progressgrid.api.repository.UserRepository;
import com.progressgrid.api.service.EmailService;
import com.progressgrid.api.service.OtpService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The auth and ownership fixes, end to end over in-memory H2. Each test pins a hole that was
 * open on main, so reintroducing one fails here instead of in production.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UserRepository users;

    @Autowired
    private OtpService otpService;

    /** Never sends real email, and lets tests read the code that would have been sent. */
    @MockBean
    private EmailService emailService;

    private record Account(String username, String email, String password, String token) {
        String bearer() {
            return "Bearer " + token;
        }
    }

    // --- helpers ---

    private Account signUp() throws Exception {
        String name = "u" + UUID.randomUUID().toString().substring(0, 8);
        String email = name + "@example.com";
        String password = "secret123";
        JsonNode body = json.readTree(mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", name, "email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        return new Account(name, email, password, body.get("token").asText());
    }

    private ResultActions login(String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", username, "password", password))));
    }

    private ResultActions postJson(String url, Object body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private long createHabit(Account owner, String name) throws Exception {
        return json.readTree(mvc.perform(post("/api/habits")
                        .header("Authorization", owner.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private ResultActions tick(Account as, long habitId) throws Exception {
        return mvc.perform(post("/api/habits/" + habitId + "/complete")
                .header("Authorization", as.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("date", LocalDate.now().toString(), "completed", true))));
    }

    // --- passwords ---

    @Test
    void signupStoresABcryptHashNotThePassword() throws Exception {
        Account a = signUp();
        String stored = users.findByUsername(a.username()).getPasswordHash();
        assertThat(stored).startsWith("$2").isNotEqualTo(a.password());
    }

    @Test
    void wrongPasswordIsRejectedExactlyLikeAnUnknownAccount() throws Exception {
        Account a = signUp();
        login(a.username(), a.password()).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());

        String wrongPassword = login(a.username(), "not-it").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknownAccount = login("nobody-" + UUID.randomUUID(), "not-it").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // JSON body (login.js used to crash reading a bare string), identical in both cases.
        assertThat(json.readTree(wrongPassword).get("message").asText()).isNotBlank();
        assertThat(wrongPassword).isEqualTo(unknownAccount);
    }

    @Test
    void legacyPlainTextAccountStillSignsInAndIsUpgradedToAHash() throws Exception {
        String name = "legacy" + UUID.randomUUID().toString().substring(0, 8);
        User legacy = new User();
        legacy.setUsername(name);
        legacy.setEmail(name + "@example.com");
        legacy.setPasswordHash("plain-old-pw");
        users.save(legacy);

        login(name, "plain-old-pw").andExpect(status().isOk());
        assertThat(users.findByUsername(name).getPasswordHash()).startsWith("$2");
        login(name, "plain-old-pw").andExpect(status().isOk());
        login(name, "plain-old-pw ").andExpect(status().isUnauthorized()); // no more whitespace-trimming match
    }

    @Test
    void signupRejectsShortPasswords() throws Exception {
        String name = "short" + UUID.randomUUID().toString().substring(0, 8);
        postJson("/api/auth/signup", Map.of("username", name, "email", name + "@example.com", "password", "12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // --- sessions ---

    @Test
    void habitsRequireAValidSessionToken() throws Exception {
        Account a = signUp();
        mvc.perform(get("/api/habits")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/habits").header("X-User-Id", 1)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/habits").header("Authorization", "Bearer forged.token.value"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/habits").header("Authorization", a.bearer())).andExpect(status().isOk());
    }

    @Test
    void oneUserCannotSeeTickOrDeleteAnothersHabit() throws Exception {
        Account alice = signUp();
        Account mallory = signUp();
        long habit = createHabit(alice, "Read");

        mvc.perform(get("/api/habits").header("Authorization", mallory.bearer()))
                .andExpect(jsonPath("$.length()").value(0));
        tick(mallory, habit).andExpect(status().isNotFound());
        mvc.perform(delete("/api/habits/" + habit).header("Authorization", mallory.bearer()))
                .andExpect(status().isNotFound());

        // Alice's habit is untouched, and she can still tick it herself.
        mvc.perform(get("/api/habits").header("Authorization", alice.bearer()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completions.length()").value(0));
        tick(alice, habit).andExpect(status().isOk());
    }

    // --- password reset ---

    @Test
    void resetByEmailNeedsAVerifiedCodeAndStoresAHash() throws Exception {
        Account a = signUp();

        // By email: the emailed code used to be overwritten by a second, never-sent one.
        postJson("/api/auth/forgot-password/send-otp", Map.of("identifier", a.email())).andExpect(status().isOk());
        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendOtpEmail(eq(a.email()), any(), sent.capture());
        String code = sent.getValue();

        Map<String, String> reset = Map.of("identifier", a.email(), "otp", code, "newPassword", "brand-new-pw");
        // The right code alone is not enough; it has to pass verify-otp first.
        postJson("/api/auth/reset-password", reset).andExpect(status().isBadRequest());

        postJson("/api/auth/forgot-password/verify-otp", Map.of("identifier", a.email(), "otp", code))
                .andExpect(status().isOk());
        postJson("/api/auth/reset-password", reset).andExpect(status().isOk());

        assertThat(users.findByUsername(a.username()).getPasswordHash()).startsWith("$2");
        login(a.username(), "brand-new-pw").andExpect(status().isOk());
        login(a.username(), a.password()).andExpect(status().isUnauthorized());
    }

    @Test
    void aResetCodeIsBurnedAfterFiveWrongGuesses() {
        String key = "guess-" + UUID.randomUUID() + "@example.com";
        String code = otpService.generateOtp(key); // always 100000-999999, so 000000 never matches

        for (int i = 0; i < 5; i++) {
            assertThat(otpService.verifyOtp(key, "000000")).isFalse();
        }
        assertThat(otpService.verifyOtp(key, code)).isFalse();
    }
}
