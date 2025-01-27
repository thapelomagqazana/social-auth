package com.example.persistence.entity;

import jakarta.persistence.*;

/**
 * Represents a User entity in the database.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * The unique identifier for the user.
     * Auto-generated by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The username of the user.
     * This field is mandatory and must be unique.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * The email address of the user.
     * This field is mandatory and must be unique.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The hashed password of the user.
     * This field is mandatory.
     */
    @Column(nullable = false)
    private String password;

    // Getters and Setters

    /**
     * Gets the user's ID.
     * @return The ID of the user.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     * @param id The ID of the user.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the username of the user.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email of the user.
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user.
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the password of the user.
     * @return The hashed password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     * @param password The hashed password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
