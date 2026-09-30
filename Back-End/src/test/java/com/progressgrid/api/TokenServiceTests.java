package com.progressgrid.api;

import com.progressgrid.api.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Session tokens have to outlive a backend restart, with or without JWT_SECRET set. */
class TokenServiceTests {

    private static final long DAY = 86_400_000L;

    @TempDir
    Path dir;

    @Test
    void withoutJwtSecretTokensSurviveARestart() {
        String secretFile = dir.resolve("jwt-secret").toString();
        String token = new TokenService("", DAY, secretFile).issue(42L);

        // A fresh instance is what a restart looks like.
        assertThat(new TokenService("", DAY, secretFile).userIdFrom(token)).isEqualTo(42L);
        assertThat(Files.exists(Path.of(secretFile))).isTrue();
    }

    @Test
    void theGeneratedSecretIsNotSharedBetweenInstallations() {
        String token = new TokenService("", DAY, dir.resolve("a").toString()).issue(42L);
        assertThat(new TokenService("", DAY, dir.resolve("b").toString()).userIdFrom(token)).isNull();
    }

    @Test
    void jwtSecretWinsOverTheLocalFile() {
        String secret = "configured-secret-at-least-32-bytes-long-0123";
        String token = new TokenService(secret, DAY, dir.resolve("jwt-secret").toString()).issue(7L);

        assertThat(new TokenService(secret, DAY, dir.resolve("other").toString()).userIdFrom(token)).isEqualTo(7L);
        assertThat(Files.exists(dir.resolve("jwt-secret"))).isFalse();
    }
}
