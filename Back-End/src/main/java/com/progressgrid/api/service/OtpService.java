package com.progressgrid.api.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final long OTP_VALIDITY_SECONDS = 600; // 10 minutes
    private static final SecureRandom random = new SecureRandom();

    private static class OtpData {
        final String otp;
        final Instant expiresAt;
        boolean verified = false;

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
     * Verify whether the entered OTP is correct and not expired
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

        if (enteredOtp != null && data.otp.equals(enteredOtp.trim())) {
            data.verified = true;
            return true;
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
        return (data.verified || (otp != null && data.otp.equals(otp.trim())));
    }

    /**
     * Invalidate and remove OTP upon password update
     */
    public void clearOtp(String identifier) {
        String key = normalize(identifier);
        otpStorage.remove(key);
    }
}
