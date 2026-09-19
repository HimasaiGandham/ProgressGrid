package com.progressgrid.api.controller;

import com.progressgrid.api.dto.*;
import com.progressgrid.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Errors are JSON {"message": ...} like the other endpoints here. A bare-string body made
    // login.js throw while reading it, and that throw fell into its offline fallback, which
    // signed the visitor in regardless of the password.

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            AuthResponseDTO response = authService.login(loginDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<?> signup(@RequestBody SignupDTO signupDTO) {
        try {
            AuthResponseDTO response = authService.signup(signupDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpDTO dto) {
        try {
            String maskedEmail = authService.sendPasswordResetOtp(dto.getIdentifier());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Verification code sent to " + maskedEmail,
                    "maskedEmail", maskedEmail
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        try {
            boolean valid = authService.verifyPasswordResetOtp(dto.getIdentifier(), dto.getOtp());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "verified", valid,
                    "message", "OTP verified successfully. You may now reset your password."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resetDTO) {
        try {
            authService.resetPassword(resetDTO);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Password has been reset successfully. You can now sign in."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
