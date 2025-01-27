package com.example.service.service;

import com.example.persistence.entity.User;
import com.example.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing User entities.
 * Contains business logic for user-related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructor to inject the UserRepository.
     *
     * @param userRepository The repository for interacting with the database.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Saves a new user to the database.
     *
     * @param user The user entity to save.
     * @return The saved user entity.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
