package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Register a new user with encrypted password and default role.
     *
     * @param user the user to register
     * @return the saved user entity
     * @throws IllegalArgumentException if the username or email is already taken
     */
    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt the password
        user.getRoles().add("USER"); // Assign default role
        return userRepository.save(user); // Save the user in the database
    }

    /**
     * Find a user by their username.
     *
     * @param username the username to search for
     * @return the user entity
     * @throws RuntimeException if the user is not found
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
