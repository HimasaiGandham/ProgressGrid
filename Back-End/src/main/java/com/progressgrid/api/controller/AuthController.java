package com.progressgrid.api.controller;

import com.progressgrid.api.dto.*;
import com.progressgrid.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Errors come back as JSON {"status": "error", "message": ...}, which login.js and the reset
 * pop-up read the message from. A failed login is 401; anything else the user got wrong is 400.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            return ResponseEntity.ok(authService.login(loginDTO));
        } catch (RuntimeException e) {
            return error(HttpStatus.UNAUTHORIZED, e);
        }
    }

    @PostMapping({"/signup", "/register"})
    public AuthResponseDTO signup(@RequestBody SignupDTO signupDTO) {
        return authService.signup(signupDTO);
    }

    @PostMapping("/forgot-password/send-otp")
    public Map<String, Object> sendOtp(@RequestBody SendOtpDTO dto) {
        String maskedEmail = authService.sendPasswordResetOtp(dto.getIdentifier());
        return Map.of("status", "success", "message", "Verification code sent to " + maskedEmail, "maskedEmail", maskedEmail);
    }

    @PostMapping("/forgot-password/verify-otp")
    public Map<String, Object> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        boolean valid = authService.verifyPasswordResetOtp(dto.getIdentifier(), dto.getOtp());
        return Map.of("status", "success", "verified", valid, "message", "OTP verified successfully. You may now reset your password.");
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody ResetPasswordDTO resetDTO) {
        authService.resetPassword(resetDTO);
        return Map.of("status", "success", "message", "Password has been reset successfully. You can now sign in.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, RuntimeException e) {
        String message = Objects.requireNonNullElse(e.getMessage(), "Something went wrong");
        return ResponseEntity.status(status).body(Map.of("status", "error", "message", message));
    }
}
