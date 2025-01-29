package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

        user.setUsername(user.getUsername().toLowerCase().trim());
        user.setEmail(user.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt the password
        user.getRoles().add("ROLE_USER"); // Assign default role
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

    /**
     * Retrieves all users from the database (for non-paginated requests).
     * @return a list of all registered users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves all users from the database with pagination and sorting.
     *
     * @param pageable Pageable object containing pagination and sorting information.
     * @return a paginated list of users.
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Retrieve a user by their ID.
     * @param id The ID of the user.
     * @return The user entity if found.
     * @throws RuntimeException If the user is not found.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
}
