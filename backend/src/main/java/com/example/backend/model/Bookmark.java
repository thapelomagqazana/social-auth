package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a bookmarked image or content saved by a user.
 */
@Data
@Entity
@Table(name = "bookmarks")
@JsonInclude(JsonInclude.Include.NON_NULL) // Prevents null values in JSON response
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required.")
    @Size(max = 100, message = "Title must not exceed 100 characters.")
    private String title;

    @NotBlank(message = "URL is required.")
    @Size(max = 500, message = "URL length must not exceed 500 characters.")
    private String url;

    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Bookmark() {}

    public Bookmark(String title, String url, User user) {
        this.title = title;
        this.url = url;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }
}