package com.progressgrid.api.service;

import com.progressgrid.api.dto.AuthResponseDTO;
import com.progressgrid.api.dto.LoginDTO;
import com.progressgrid.api.dto.ResetPasswordDTO;
import com.progressgrid.api.dto.SignupDTO;
import com.progressgrid.api.model.User;
import com.progressgrid.api.repository.UserRepository;
import com.progressgrid.api.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponseDTO login(LoginDTO loginDTO) {
        String query = loginDTO.getUsername() != null ? loginDTO.getUsername().trim() : "";
        String inputPassword = loginDTO.getPassword() != null ? loginDTO.getPassword() : "";

        if (query.isEmpty() || inputPassword.isEmpty()) {
            throw new RuntimeException("Username and password are required");
        }

        for (User user : userRepository.findMatchingUsers(query)) {
            if (passwordMatches(user, inputPassword)) {
                return toResponse(user);
            }
        }
        // One message for "no such account" and "wrong password", so login can't be used to probe for accounts.
        throw new RuntimeException("Invalid username or password");
    }

    public AuthResponseDTO signup(SignupDTO signupDTO) {
        String username = signupDTO.getUsername() != null ? signupDTO.getUsername().trim() : "";
        String email = signupDTO.getEmail() != null ? signupDTO.getEmail().trim() : "";
        String password = signupDTO.getPassword() != null ? signupDTO.getPassword() : "";

        if (username.isEmpty() || email.isEmpty()) {
            throw new RuntimeException("Username and email are required");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userRepository.findByUsername(username) != null || userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setName(signupDTO.getName() != null ? signupDTO.getName() : username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));

        return toResponse(userRepository.save(user));
    }

    public String sendPasswordResetOtp(String identifier) {
        String query = identifier != null ? identifier.trim() : "";
        if (query.isEmpty()) {
            throw new RuntimeException("Username or email is required");
        }

        User targetUser = findAccount(query);
        if (targetUser == null) {
            throw new RuntimeException("No account found with this username, email or ID");
        }

        // The code is stored under the account's email only. It used to be generated a second time
        // under the typed identifier, which overwrote the emailed code whenever that was the email.
        String otp = otpService.generateOtp(targetUser.getEmail());
        emailService.sendOtpEmail(targetUser.getEmail(), targetUser.getUsername(), otp);

        return maskEmail(targetUser.getEmail());
    }

    public boolean verifyPasswordResetOtp(String identifier, String otp) {
        String query = identifier != null ? identifier.trim() : "";
        if (query.isEmpty() || otp == null || otp.trim().isEmpty()) {
            throw new RuntimeException("Identifier and OTP code are required");
        }

        User user = findAccount(query);
        if (user != null && otpService.verifyOtp(user.getEmail(), otp)) {
            return true;
        }

        throw new RuntimeException("Invalid or expired OTP code. Please check your email or request a new code.");
    }

    public void resetPassword(ResetPasswordDTO dto) {
        String query = dto.getIdentifier();
        if (query == null || query.trim().isEmpty()) {
            throw new RuntimeException("Email or username is required");
        }
        String otp = dto.getOtp();
        String newPassword = dto.getNewPassword();

        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        User targetUser = findAccount(query.trim());
        if (targetUser == null) {
            throw new RuntimeException("Account not found with this email or username");
        }

        // Security check: the code must already have passed verify-otp
        if (!otpService.isVerified(targetUser.getEmail(), otp)) {
            throw new RuntimeException("Security verification failed: Valid OTP is required before resetting password.");
        }

        targetUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(targetUser);

        // Invalidate OTP immediately after successful reset
        otpService.clearOtp(targetUser.getEmail());
    }

    /**
     * BCrypt check. Accounts created before passwords were hashed (including the seed data) still
     * hold plain text; an exact match on one of those is accepted once and re-saved as a hash.
     */
    private boolean passwordMatches(User user, String inputPassword) {
        String stored = user.getPasswordHash();
        if (stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(inputPassword, stored);
        }
        if (MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8),
                inputPassword.getBytes(StandardCharsets.UTF_8))) {
            user.setPasswordHash(passwordEncoder.encode(inputPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /** Resolves a username, email or numeric account id to its user, or null if none matches. */
    private User findAccount(String query) {
        List<User> users = userRepository.findMatchingUsers(query);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        try {
            return userRepository.findById(Long.parseLong(query)).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private AuthResponseDTO toResponse(User user) {
        return new AuthResponseDTO(user.getId(), user.getUsername(), user.getEmail(), tokenService.issue(user.getId()));
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.substring(0, 2) + "***@" + domain;
    }
}
