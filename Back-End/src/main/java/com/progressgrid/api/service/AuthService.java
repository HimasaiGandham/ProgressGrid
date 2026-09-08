package com.progressgrid.api.service;

import com.progressgrid.api.dto.AuthResponseDTO;
import com.progressgrid.api.dto.LoginDTO;
import com.progressgrid.api.dto.SignupDTO;
import com.progressgrid.api.model.User;
import com.progressgrid.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public AuthResponseDTO login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername());
        if (user == null || !user.getPasswordHash().equals(loginDTO.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        return new AuthResponseDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    public AuthResponseDTO signup(SignupDTO signupDTO) {
        if (userRepository.findByUsername(signupDTO.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setEmail(signupDTO.getEmail());
        user.setPasswordHash(signupDTO.getPassword()); // Plain text for simplicity, as no Spring Security added
        
        User savedUser = userRepository.save(user);
        return new AuthResponseDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }
}
