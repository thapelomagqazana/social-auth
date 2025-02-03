package com.example.backend.bookmark;

import com.example.backend.model.Bookmark;
import com.example.backend.model.User;
import com.example.backend.service.BookmarkService;
import com.example.backend.service.UserService;
import com.example.backend.security.JwtUtils;
import com.example.backend.security.UserDetailsServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GetBookmarkByIdControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private BookmarkService bookmarkService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    
    private String userToken, expiredToken, invalidToken, disabledUserToken;
    private User normalUser, disabledUser;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final String USER_ROLE = "USER";

    @BeforeEach
    void setUp() {
        normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("hashedpassword"), Set.of(USER_ROLE));
        disabledUser = new User(6L, "disabledUser", "disabled@example.com", passwordEncoder.encode("hashedpassword"), Set.of(USER_ROLE));
        disabledUser.setEnabled(false);

        // Generate JWT Tokens
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, "USER");
        expiredToken = "expired.jwt.token";
        invalidToken = "invalid.jwt.token";
        disabledUserToken = jwtUtils.generateToken(disabledUser.getUsername(), 86400000, "USER");

        Mockito.when(userService.findByUsername("normalUser")).thenReturn(normalUser);
        Mockito.when(userService.findByUsername("disabledUser")).thenReturn(disabledUser);

        // Mock UserDetailsService
        Mockito.when(userDetailsService.loadUserByUsername("normalUser"))
        .thenReturn(org.springframework.security.core.userdetails.User.withUsername("normalUser")
            .password(normalUser.getPassword())
            .roles(USER_ROLE)
            .build());

        Mockito.when(userDetailsService.loadUserByUsername("disabledUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("disabledUser")
                .password(normalUser.getPassword())
                .roles(USER_ROLE)
                .build());

        Mockito.when(bookmarkService.getBookmarkByIdAndUser(Mockito.anyLong(), Mockito.eq(normalUser)))
            .thenAnswer(invocation -> {
                Long bookmarkId = invocation.getArgument(0);
                if (bookmarkId == 1L) {
                    return Optional.of(new Bookmark("First Bookmark", "https://example.com/first", normalUser));
                } else if (bookmarkId == 10L) {
                    return Optional.of(new Bookmark("Existing Bookmark", "https://example.com/existing", normalUser));
                } else if (bookmarkId == 15L) {
                    return Optional.of(new Bookmark("Special ✨ Title!", "https://example.com/special", normalUser));
                } else if (bookmarkId == 20L) {
                    return Optional.of(new Bookmark("Long URL Bookmark", "https://example.com/" + "a".repeat(499), normalUser));
                } else {
                    return Optional.empty();
                }
            });

        // Mock Bookmark Service for a missing bookmark
        Mockito.when(bookmarkService.getBookmarkByIdAndUser(9999L, normalUser)).thenReturn(Optional.empty());
        Mockito.when(bookmarkService.getBookmarkByIdAndUser(10L, disabledUser)).thenReturn(Optional.empty());
    }
    /**
     * TC_POS_001: Fetch an existing bookmark
     */
    @Test
    void testFetchExistingBookmark() throws Exception {
        mockMvc.perform(get("/api/bookmarks/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing Bookmark"))
                .andExpect(jsonPath("$.url").value("https://example.com/existing"));
    }

    /**
     * TC_POS_002: Fetch the first bookmark created by the user
     */
    @Test
    void testFetchFirstBookmark() throws Exception {
        mockMvc.perform(get("/api/bookmarks/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First Bookmark"))
                .andExpect(jsonPath("$.url").value("https://example.com/first"));
    }

    /**
     * TC_POS_003: Fetch the latest bookmark created by the user
     */
    @Test
    void testFetchLatestBookmark() throws Exception {
        mockMvc.perform(get("/api/bookmarks/20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Long URL Bookmark"))
                .andExpect(jsonPath("$.url").value("https://example.com/" + "a".repeat(499)));
    }

    /**
     * TC_POS_004: Fetch a bookmark with special characters in the title
     */
    @Test
    void testFetchBookmarkWithSpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/bookmarks/15")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Special ✨ Title!"))
                .andExpect(jsonPath("$.url").value("https://example.com/special"));
    }

    /**
     * TC_POS_005: Fetch a bookmark with a long URL (499 characters)
     */
    @Test
    void testFetchBookmarkWithLongURL() throws Exception {
        mockMvc.perform(get("/api/bookmarks/20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Long URL Bookmark"))
                .andExpect(jsonPath("$.url").value("https://example.com/" + "a".repeat(499)));
    }

    /**
     * TC_NEG_001: No authentication token provided
     */
    @Test
    void testFetchWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/bookmarks/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid JWT token used
     */
    @Test
    void testFetchWithInvalidJWT() throws Exception {
        mockMvc.perform(get("/api/bookmarks/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: JWT token has expired
     */
    @Test
    void testFetchWithExpiredJWT() throws Exception {
        mockMvc.perform(get("/api/bookmarks/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_004: User account is disabled
     */
    @Test
    void testFetchWithDisabledUser() throws Exception {
        mockMvc.perform(get("/api/bookmarks/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + disabledUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User account is disabled"));
    }

    /**
     * TC_NEG_005: Bookmark does not exist
     */
    @Test
    void testFetchNonExistentBookmark() throws Exception {
        mockMvc.perform(get("/api/bookmarks/9999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bookmark not found"));
    }

    /**
     * TC_NEG_007: User is deleted but still has a valid JWT
     */
    @Test
    void testFetchBookmarkWithDeletedUser() throws Exception {
        Mockito.when(userService.findByUsername("deletedUser")).thenReturn(null);
        String deletedUserToken = jwtUtils.generateToken("deletedUser", 86400000, "USER");

        mockMvc.perform(get("/api/bookmarks/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deletedUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User no longer exists."));
    }

    /**
     * TC_NEG_008: Bookmark ID is negative
     */
    @Test
    void testFetchBookmarkWithNegativeId() throws Exception {
        mockMvc.perform(get("/api/bookmarks/-5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid bookmark ID"));
    }

    /**
     * TC_NEG_009: Bookmark ID is not a number
     */
    @Test
    void testFetchBookmarkWithNonNumericId() throws Exception {
        mockMvc.perform(get("/api/bookmarks/abc")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid bookmark ID"));
    }
}
