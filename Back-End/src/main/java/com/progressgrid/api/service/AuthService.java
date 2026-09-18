package com.progressgrid.api.service;

import com.progressgrid.api.dto.AuthResponseDTO;
import com.progressgrid.api.dto.LoginDTO;
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
                // 1. Direct plain text match
                if (stored.equals(inputPassword) || stored.trim().equals(inputPassword.trim())) {
                    authenticatedUser = user;
                    break;
                }
                // 2. BCrypt hash verification
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
            throw new RuntimeException("Invalid password credentials");
        }

        return new AuthResponseDTO(
                authenticatedUser.getId(),
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail()
        );
    }

    public AuthResponseDTO signup(SignupDTO signupDTO) {
        if (signupDTO.getUsername() == null || signupDTO.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (signupDTO.getEmail() == null || signupDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (signupDTO.getPassword() == null || signupDTO.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (userRepository.findByUsername(signupDTO.getUsername().trim()) != null) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.findByEmail(signupDTO.getEmail().trim()) != null) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setUsername(signupDTO.getUsername().trim());
        user.setEmail(signupDTO.getEmail().trim());
        user.setPasswordHash(signupDTO.getPassword());

        user = userRepository.save(user);

        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
