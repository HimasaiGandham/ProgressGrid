package com.progressgrid.api.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
                        @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            this.key = Jwts.SIG.HS256.key().build();
            log.warn("JWT_SECRET is not set - using a random signing key, so everyone is signed out "
                    + "whenever the backend restarts. Set JWT_SECRET (32+ characters) in production.");
        } else {
            // Throws WeakKeyException at startup if the secret is shorter than 32 bytes.
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.expirationMs = expirationMs;
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
