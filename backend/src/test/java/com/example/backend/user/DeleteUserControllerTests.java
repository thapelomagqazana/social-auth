package com.example.backend.user;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for DELETE /api/users/{id} - Admin Deletes a User.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeleteUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private String adminToken;
    private String userToken;
    private String expiredAdminToken;
    private String invalidToken = "InvalidJWTTokenString";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Mock Admin User
        User adminUser = new User(1L, "adminUser", "admin@example.com", passwordEncoder.encode("Admin@123"), Set.of("ADMIN"));
        // Mock Standard User
        User normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("User@123"), Set.of("USER"));
        // Mock Deleted User
        User deletedUser = new User(20L, "deletedUser", "deleted@example.com", passwordEncoder.encode("Deleted@123"), Set.of("ADMIN"));

        // Mock Users to be deleted
        User userToDelete1 = new User(10L, "deleteUser1", "delete1@example.com", "password", Set.of("USER"));
        User userToDelete2 = new User(15L, "john_doe@2024!", "delete2@example.com", "password", Set.of("USER"));
        User userToDelete3 = new User(12L, "long_username_user_2024", "delete3@example.com", "password", Set.of("USER"));
        User userToDelete4 = new User(8L, "multiRoleUser", "multi@example.com", "password", Set.of("USER", "ADMIN"));
        User userToDelete5 = new User(20L, "recentUser", "recent@example.com", "password", Set.of("USER"));

        // Generate JWT Tokens
        adminToken = jwtUtils.generateToken(adminUser.getUsername(), 86400000, "ADMIN");
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, "USER");

        // Mock Repository
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(userToDelete1));
        Mockito.when(userRepository.findById(15L)).thenReturn(Optional.of(userToDelete2));
        Mockito.when(userRepository.findById(12L)).thenReturn(Optional.of(userToDelete3));
        Mockito.when(userRepository.findById(8L)).thenReturn(Optional.of(userToDelete4));
        Mockito.when(userRepository.findById(20L)).thenReturn(Optional.of(userToDelete5));
        Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(new User(10L, "deleteUser", "delete@example.com", "password", Set.of("USER"))));
        Mockito.when(userRepository.findById(8L)).thenReturn(Optional.of(new User(8L, "targetUser", "target@example.com", "password", Set.of("USER"))));
        Mockito.when(userRepository.findById(30L)).thenReturn(Optional.empty()); // Deleted user
        Mockito.when(userRepository.findById(9999L)).thenReturn(Optional.empty()); // Non-existent user

        // Mock UserDetailsService for Authentication
        Mockito.when(userDetailsService.loadUserByUsername("adminUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("adminUser")
                    .password(adminUser.getPassword())
                    .roles("ADMIN")
                    .build());

        Mockito.when(userDetailsService.loadUserByUsername("normalUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("normalUser")
                    .password(normalUser.getPassword())
                    .roles("USER")
                    .build());
    }
    /**
     * TC_POS_001: Admin successfully deletes a user.
     */
    @Test
    void testAdminDeletesUserSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));
    }

    /**
     * TC_POS_002: Admin deletes a user with special characters in username.
     */
    @Test
    void testAdminDeletesUserWithSpecialCharacters() throws Exception {
        mockMvc.perform(delete("/api/users/15")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));
    }

    /**
     * TC_POS_003: Admin deletes a user with a long username.
     */
    @Test
    void testAdminDeletesUserWithLongUsername() throws Exception {
        mockMvc.perform(delete("/api/users/12")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));
    }

    /**
     * TC_POS_004: Admin deletes a user with multiple roles.
     */
    @Test
    void testAdminDeletesUserWithMultipleRoles() throws Exception {
        mockMvc.perform(delete("/api/users/8")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));
    }

    /**
     * TC_POS_005: Admin deletes a recently created user.
     */
    @Test
    void testAdminDeletesRecentlyCreatedUser() throws Exception {
        mockMvc.perform(delete("/api/users/20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthTokenProvided() throws Exception {
        mockMvc.perform(delete("/api/users/10") // No Authorization header
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid authentication token used.
     */
    @Test
    void testInvalidTokenUsed() throws Exception {
        mockMvc.perform(delete("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: Standard user tries to delete another user.
     */
    @Test
    void testUserDeletingAnotherUser() throws Exception {
        mockMvc.perform(delete("/api/users/8")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    /**
     * TC_NEG_004: Standard user tries to delete an admin.
     */
    // @Test
    // void testUserDeletingAdmin() throws Exception {
    //     mockMvc.perform(delete("/api/users/1")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isForbidden())
    //             .andExpect(jsonPath("$.message").value("Access denied."));
    // }

    // /**
    //  * TC_NEG_005: Token from a deleted user is used.
    //  */
    // @Test
    // void testTokenFromDeletedUser() throws Exception {
    //     mockMvc.perform(delete("/api/users/30")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(jsonPath("$.message").value("User no longer exists."));
    // }

    /**
     * TC_NEG_006: Expired token is used.
     */
    @Test
    void testExpiredTokenUsed() throws Exception {
        mockMvc.perform(delete("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredAdminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_007: JWT token from another system/app used.
     */
    @Test
    void testTokenFromAnotherSystem() throws Exception {
        String foreignToken = jwtUtils.generateToken("hacker", 86400000, "ROLE_UNKNOWN");

        mockMvc.perform(delete("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User no longer exists."));
    }

    // /**
    //  * TC_NEG_008: Request with manipulated JWT payload.
    //  */
    // @Test
    // void testManipulatedJwtPayload() throws Exception {
    //     String manipulatedToken = jwtUtils.generateToken("adminUser", 86400000, "USER"); // Changed role

    //     mockMvc.perform(delete("/api/users/10")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + manipulatedToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isForbidden())
    //             .andExpect(jsonPath("$.message").value("Access denied."));
    // }

    /**
     * TC_NEG_009: Admin tries to delete themselves.
     */
    @Test
    void testAdminDeletingThemselves() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Admin cannot delete themselves."));
    }

    /**
     * TC_NEG_010: Admin tries to delete a non-existent user.
     */
    @Test
    void testAdminDeletingNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/users/9999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to delete user."));
    }
}
