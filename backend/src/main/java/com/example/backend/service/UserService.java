package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    
    /**
     * Updates the user's profile dynamically based on the given fields.
     *
     * @param id      The ID of the user to update.
     * @param updates A map of fields to update.
     * @return The updated user.
     */
    public User updateUser(Long id, Map<String, Object> updates, boolean isAdmin) {
        return userRepository.findById(id)
            .map(user -> applyUpdates(user, updates, isAdmin))
            .map(userRepository::save)
            .orElse(null);
    }
    
    /**
     * Applies updates to the user dynamically.
     */
    private User applyUpdates(User user, Map<String, Object> updates, boolean isAdmin) {
        updates.forEach((key, value) -> {
            switch (key) {
                case "username":
                    user.setUsername(validateUsername(value.toString(), user.getUsername()));
                    break;
                case "email":
                    user.setEmail(validateEmail(value.toString(), user.getEmail()));
                    break;
                case "password":
                    user.setPassword(validatePassword(value.toString()));
                    break;
                case "roles":
                    if (isAdmin) {
                        user.setRoles(convertRoles(value));
                    }
                    break;
            }
        });
        return user;
    }
    
    /**
     * Validates the new username.
     */
    private String validateUsername(String newUsername, String currentUsername) {
        if (newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (newUsername.length() > 255) {
            throw new IllegalArgumentException("Field length exceeds the limit");
        }
        if (userRepository.findByUsername(newUsername).isPresent() && !currentUsername.equals(newUsername)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        return newUsername;
    }
    
    /**
     * Validates the new email.
     */
    private String validateEmail(String newEmail, String currentEmail) {
        if (!newEmail.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (newEmail.length() > 255) {
            throw new IllegalArgumentException("Field length exceeds the limit");
        }
        if (userRepository.findByEmail(newEmail).isPresent() && !currentEmail.equals(newEmail)) {
            throw new IllegalArgumentException("Email is already in use");
        }
        return newEmail;
    }
    
    /**
     * Validates and hashes the new password.
     */
    private String validatePassword(String newPassword) {
        if (newPassword.length() < 8 || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password is too weak");
        }
        return encodePassword(newPassword);
    }
    
    /**
     * Converts roles from a List to a Set safely.
     */
    @SuppressWarnings("unchecked")
    private Set<String> convertRoles(Object roles) {
        if (roles instanceof List) {
            return new HashSet<>((List<String>) roles);
        }
        throw new IllegalArgumentException("Invalid roles format");
    }

    /**
     * Deletes a user by ID (Admin only).
     *
     * @param id The ID of the user to delete.
     * @param loggedInUsername The username of the admin making the request.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteUser(Long id, String loggedInUsername) {
        Optional<User> optionalUser = userRepository.findById(id);
    
        if (optionalUser.isEmpty()) {
            return false;
        }
    
        User userToDelete = optionalUser.get();
    
        // Prevent admins from deleting themselves
        if (userToDelete.getUsername().equals(loggedInUsername)) {
            throw new IllegalArgumentException("Admin cannot delete themselves.");
        }
    
        try {
            userRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user due to an internal error.");
        }
    }
}
