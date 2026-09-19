package com.progressgrid.api.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OtpService {

    private static final long OTP_VALIDITY_SECONDS = 600; // 10 minutes
    /** Guesses allowed per code. Uncapped, a 6-digit code can be brute-forced well within 10 minutes. */
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom random = new SecureRandom();

    private static class OtpData {
        final String otp;
        final Instant expiresAt;
        final AtomicInteger attempts = new AtomicInteger();
        volatile boolean verified = false;

        OtpData(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private String normalize(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase();
    }

    /**
     * Generate a 6-digit OTP for the given identifier
     */
    public String generateOtp(String identifier) {
        String key = normalize(identifier);
        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);
        otpStorage.put(key, new OtpData(otp, Instant.now().plusSeconds(OTP_VALIDITY_SECONDS)));
        return otp;
    }

    /**
     * Verify whether the entered OTP is correct and not expired. After MAX_ATTEMPTS guesses the
     * code is discarded and the user has to request a new one.
     */
    public boolean verifyOtp(String identifier, String enteredOtp) {
        String key = normalize(identifier);
        OtpData data = otpStorage.get(key);

        if (data == null) {
            return false;
        }

        if (data.isExpired()) {
            otpStorage.remove(key);
            return false;
        }

        // Count the attempt before comparing, so concurrent guesses can't slip past the cap.
        int attempt = data.attempts.incrementAndGet();
        if (attempt > MAX_ATTEMPTS) {
            otpStorage.remove(key);
            return false;
        }

        if (enteredOtp != null && matches(data.otp, enteredOtp.trim())) {
            data.verified = true;
            return true;
        }

        if (attempt == MAX_ATTEMPTS) {
            otpStorage.remove(key);
        }
        return false;
    }

    /**
     * Check if an OTP was successfully verified for this identifier
     */
    public boolean isVerified(String identifier, String otp) {
        String key = normalize(identifier);
        OtpData data = otpStorage.get(key);
        if (data == null || data.isExpired()) {
            return false;
        }
        // Only a code that already passed verifyOtp counts. Accepting a bare match here let
        // reset-password be used to guess codes without ever hitting the attempt limit.
        return data.verified && otp != null && matches(data.otp, otp.trim());
    }

    /**
     * Invalidate and remove OTP upon password update
     */
    public void clearOtp(String identifier) {
        String key = normalize(identifier);
        otpStorage.remove(key);
    }

    private static boolean matches(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
