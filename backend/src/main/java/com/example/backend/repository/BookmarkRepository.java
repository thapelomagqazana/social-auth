package com.example.backend.repository;

import com.example.backend.model.Bookmark;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing bookmark data persistence.
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUser(User user); // Fetch all bookmarks for a user

    Optional<Bookmark> findByIdAndUser(Long id, User user); // Fetch a bookmark by ID and user

    boolean existsByUserAndTitleAndUrl(User user, String title, String url); // Prevent Duplicates
}
