package com.progressgrid.api.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/**
 * Issues and checks the signed session tokens that identify the caller. The API used to trust
 * a user id the browser sent in an X-User-Id header, so anyone could act as anyone.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final SecretKey key;
    private final long expirationMs;

    public TokenService(@Value("${app.jwt.secret:}") String secret,
                        @Value("${app.jwt.expiration-ms:86400000}") long expirationMs,
                        @Value("${app.jwt.secret-file:${user.home}/.progressgrid/jwt-secret}") String secretFile) {
        if (secret == null || secret.isBlank()) {
            secret = localSecret(Path.of(secretFile));
        }
        // Throws WeakKeyException at startup if the secret is shorter than 32 bytes.
        this.key = secret != null
                ? Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))
                : Jwts.SIG.HS256.key().build();
        this.expirationMs = expirationMs;
    }

    /**
     * Without JWT_SECRET, a random secret is generated once and kept in a local file, so tokens
     * survive a backend restart. There is deliberately no built-in default secret: a default in
     * the source would be public, and anyone could sign tokens for any account with it.
     * Returns null (random in-memory key) only if the file can't be read or written.
     */
    private static String localSecret(Path file) {
        try {
            if (!Files.exists(file)) {
                byte[] random = new byte[48];
                new SecureRandom().nextBytes(random);
                Files.createDirectories(file.toAbsolutePath().getParent());
                Files.writeString(file, Base64.getEncoder().encodeToString(random));
                file.toFile().setReadable(false, false);
                file.toFile().setReadable(true, true);
                log.info("JWT_SECRET is not set - generated a local signing key in {}. "
                        + "Set JWT_SECRET (32+ characters) in production.", file);
            }
            return Files.readString(file).trim();
        } catch (IOException | RuntimeException e) {
            log.warn("JWT_SECRET is not set and {} is not usable ({}) - using a random signing key, so "
                    + "everyone is signed out whenever the backend restarts.", file, e.getMessage());
            return null;
        }
    }

    public String issue(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** The user id a valid token was issued to, or null if the token is missing, forged or expired. */
    public Long userIdFrom(String token) {
        try {
            return Long.valueOf(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
