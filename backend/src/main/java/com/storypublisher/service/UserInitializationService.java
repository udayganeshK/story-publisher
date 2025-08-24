package com.storypublisher.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storypublisher.config.AuthorConfig;
import com.storypublisher.model.Role;
import com.storypublisher.model.User;
import com.storypublisher.repository.UserRepository;

import jakarta.annotation.PostConstruct;

/**
 * Service to initialize default user profile
 */
@Service
@Transactional
public class UserInitializationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthorConfig authorConfig;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Initialize or update the default author profile
     */
    @PostConstruct
    public void initializeDefaultUser() {
        try {
            Optional<User> existingUserOpt = userRepository.findByEmail(authorConfig.getEmail());
            
            if (existingUserOpt.isPresent()) {
                // Update existing user with new name information
                User existingUser = existingUserOpt.get();
                updateUserProfile(existingUser);
                System.out.println("Updated existing user profile: " + existingUser.getUsername() + 
                                 " -> " + authorConfig.getFirstName() + " " + authorConfig.getLastName());
            } else {
                // Create new default user if none exists
                createDefaultUser();
                System.out.println("Created new default user: " + authorConfig.getUsername() + 
                                 " (" + authorConfig.getFirstName() + " " + authorConfig.getLastName() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error initializing default user: " + e.getMessage());
            // Don't throw exception to prevent application startup failure
        }
    }
    
    private void updateUserProfile(User user) {
        user.setFirstName(authorConfig.getFirstName());
        user.setLastName(authorConfig.getLastName());
        user.setBio(authorConfig.getBio());
        
        // Update username if it's not already the desired one
        if (!authorConfig.getUsername().equals(user.getUsername()) && 
            !userRepository.existsByUsername(authorConfig.getUsername())) {
            user.setUsername(authorConfig.getUsername());
        }
        
        userRepository.save(user);
    }
    
    private void createDefaultUser() {
        User defaultUser = new User();
        defaultUser.setUsername(authorConfig.getUsername());
        defaultUser.setEmail(authorConfig.getEmail());
        defaultUser.setFirstName(authorConfig.getFirstName());
        defaultUser.setLastName(authorConfig.getLastName());
        defaultUser.setBio(authorConfig.getBio());
        defaultUser.setRole(Role.USER);
        defaultUser.setPassword(passwordEncoder.encode("defaultpassword123")); // Should be changed on first login
        
        userRepository.save(defaultUser);
    }
}
