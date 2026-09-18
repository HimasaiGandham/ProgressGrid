package com.progressgrid.api.service;

import com.progressgrid.api.dto.AuthResponseDTO;
import com.progressgrid.api.dto.LoginDTO;
import com.progressgrid.api.dto.ResetPasswordDTO;
import com.progressgrid.api.dto.SignupDTO;
import com.progressgrid.api.model.User;
import com.progressgrid.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponseDTO login(LoginDTO loginDTO) {
        String query = loginDTO.getUsername() != null ? loginDTO.getUsername().trim() : "";
        String inputPassword = loginDTO.getPassword() != null ? loginDTO.getPassword() : "";

        if (query.isEmpty() || inputPassword.isEmpty()) {
            throw new RuntimeException("Username and password are required");
        }

        List<User> matches = userRepository.findMatchingUsers(query);
        if (matches.isEmpty()) {
            throw new RuntimeException("Account not found with this username or email");
        }

        User authenticatedUser = null;
        for (User user : matches) {
            String stored = user.getPasswordHash();
            if (stored != null) {
                // 1. Direct plain text match (with or without trailing whitespace)
                if (stored.equals(inputPassword) || stored.trim().equals(inputPassword.trim())) {
                    authenticatedUser = user;
                    break;
                }
                // 2. BCrypt hash verification (for hashed accounts)
                if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                    try {
                        if (passwordEncoder.matches(inputPassword, stored) || passwordEncoder.matches(inputPassword.trim(), stored)) {
                            authenticatedUser = user;
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        if (authenticatedUser == null) {
            throw new RuntimeException("Incorrect password");
        }

        return new AuthResponseDTO(authenticatedUser.getId(), authenticatedUser.getUsername(), authenticatedUser.getEmail());
    }

    public AuthResponseDTO signup(SignupDTO signupDTO) {
        String username = signupDTO.getUsername() != null ? signupDTO.getUsername().trim() : "";
        String email = signupDTO.getEmail() != null ? signupDTO.getEmail().trim() : "";
        String password = signupDTO.getPassword() != null ? signupDTO.getPassword() : "";

        if (userRepository.findByUsername(username) != null || userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Username or email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setName(signupDTO.getName() != null ? signupDTO.getName() : username);
        user.setEmail(email);
        user.setPasswordHash(password);
        
        User savedUser = userRepository.save(user);
        return new AuthResponseDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    public String sendPasswordResetOtp(String identifier) {
        String query = identifier != null ? identifier.trim() : "";
        if (query.isEmpty()) {
            throw new RuntimeException("Username or email is required");
        }

        List<User> users = userRepository.findMatchingUsers(query);
        if (users.isEmpty()) {
            try {
                Long uid = Long.parseLong(query);
                userRepository.findById(uid).ifPresent(users::add);
            } catch (NumberFormatException ignored) {}
        }

        if (users.isEmpty()) {
            throw new RuntimeException("No account found with this username, email or ID");
        }

        User targetUser = users.get(0);
        String otp = otpService.generateOtp(targetUser.getEmail());
        // Also register under query/username for flexible verification
        otpService.generateOtp(query);

        emailService.sendOtpEmail(targetUser.getEmail(), targetUser.getUsername(), otp);

        return maskEmail(targetUser.getEmail());
    }

    public boolean verifyPasswordResetOtp(String identifier, String otp) {
        String query = identifier != null ? identifier.trim() : "";
        if (query.isEmpty() || otp == null || otp.trim().isEmpty()) {
            throw new RuntimeException("Identifier and OTP code are required");
        }

        // Try direct identifier first
        if (otpService.verifyOtp(query, otp)) {
            return true;
        }

        // Also try resolving user's email if identifier was username/id
        List<User> users = userRepository.findMatchingUsers(query);
        if (users.isEmpty()) {
            try {
                Long uid = Long.parseLong(query);
                userRepository.findById(uid).ifPresent(users::add);
            } catch (NumberFormatException ignored) {}
        }

        if (!users.isEmpty()) {
            User user = users.get(0);
            if (otpService.verifyOtp(user.getEmail(), otp)) {
                return true;
            }
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

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        List<User> users = userRepository.findMatchingUsers(query.trim());
        if (users.isEmpty()) {
            try {
                Long uid = Long.parseLong(query.trim());
                userRepository.findById(uid).ifPresent(users::add);
            } catch (NumberFormatException ignored) {}
        }

        if (users.isEmpty()) {
            throw new RuntimeException("Account not found with this email or username");
        }

        User targetUser = users.get(0);

        // Security check: Must have a valid verified OTP for either the query or the user's email
        boolean verified = otpService.isVerified(query, otp) || otpService.isVerified(targetUser.getEmail(), otp);
        if (!verified) {
            throw new RuntimeException("Security verification failed: Valid OTP is required before resetting password.");
        }

        // Update password
        targetUser.setPasswordHash(newPassword.trim());
        userRepository.save(targetUser);

        // Invalidate OTP immediately after successful reset
        otpService.clearOtp(query);
        otpService.clearOtp(targetUser.getEmail());
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
