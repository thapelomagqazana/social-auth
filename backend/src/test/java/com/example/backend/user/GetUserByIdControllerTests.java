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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GET /api/users/{id} - Fetch a specific user's profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GetUserByIdControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService; //Mock UserDetailsService

    private String adminToken;
    private String userToken;
    private String expiredAdminToken;
    private String invalidToken = "InvalidJWTTokenString";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final String ADMIN_ROLE = "ADMIN";
    private final String USER_ROLE = "USER";
    
    @BeforeEach
    void setUp() {
        // Setup mock admin user
        User adminUser = new User(1L, "adminUser", "admin@example.com", passwordEncoder.encode("Admin@123"), Set.of(ADMIN_ROLE));
        User normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("User@123"), Set.of(USER_ROLE));

        // Setup mock user with multiple roles
        User multiRoleUser = new User(7L, "multiRoleUser", "multi@example.com", passwordEncoder.encode("Password@123"), Set.of(ADMIN_ROLE, USER_ROLE));

        // Setup mock user with special characters in username
        User specialCharUser = new User(15L, "john-doe", "johndoe@example.com", passwordEncoder.encode("Password@123"), Set.of(ADMIN_ROLE));

        // Generate JWT tokens
        adminToken = jwtUtils.generateToken(adminUser.getUsername(), 86400000, ADMIN_ROLE);
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, USER_ROLE);

        User testUser8 = new User(8L, "testUser8", "test8@example.com", passwordEncoder.encode("Password@123"), Set.of(USER_ROLE));
        User testUser10 = new User(10L, "testUser10", "test10@example.com", passwordEncoder.encode("Password@123"), Set.of(USER_ROLE));
        User deletedUser = new User(20L, "deletedUser", "deleted@example.com", passwordEncoder.encode("Password@123"), Set.of(ADMIN_ROLE));

        // Mock user repository correctly
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findById(7L)).thenReturn(Optional.of(multiRoleUser));
        Mockito.when(userRepository.findById(15L)).thenReturn(Optional.of(specialCharUser));

        Mockito.when(userRepository.findById(8L)).thenReturn(Optional.of(testUser8));
        Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(testUser10));
        Mockito.when(userRepository.findById(20L)).thenReturn(Optional.empty());

        // Mock UserDetailsService
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

        Mockito.when(userDetailsService.loadUserByUsername("multiRoleUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("multiRoleUser")
                    .password(multiRoleUser.getPassword())
                    .roles("ADMIN", "USER")
                    .build());

        Mockito.when(userDetailsService.loadUserByUsername("john-doe"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("john-doe")
                    .password(specialCharUser.getPassword())
                    .roles("ADMIN")
                    .build());

        Mockito.when(userRepository.findById(20L)).thenReturn(Optional.empty());
        Mockito.when(userDetailsService.loadUserByUsername("deletedUser"))
                .thenThrow(new RuntimeException("User Not Found: deletedUser"));
    }

    /**
     * TC_POS_001: Fetch an existing user profile as an admin.
     */
    @Test
    void testFetchExistingUserAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users/1") // Using adminUser.getId()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminUser"));
    }

    /**
     * TC_POS_002: Fetch own user profile as a standard user.
     */
    @Test
    void testFetchOwnProfileAsUser() throws Exception {
        mockMvc.perform(get("/api/users/5") // Using normalUser.getId()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("normalUser"));
    }

    /**
     * TC_POS_003: Fetch an admin profile as another admin.
     */
    @Test
    void testFetchAdminProfileAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users/2") // Fetch another admin by ID
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminUser"));
    }

    /**
     * TC_POS_004: Fetch a user profile where the user has multiple roles.
     */
    @Test
    void testFetchUserWithMultipleRoles() throws Exception {
        mockMvc.perform(get("/api/users/7")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2));
    }

    /**
     * TC_POS_005: Fetch a profile when username contains special characters.
     */
    @Test
    void testFetchUserWithSpecialCharactersInUsername() throws Exception {
        mockMvc.perform(get("/api/users/15")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john-doe"));
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users/10") // No Authorization header
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid authentication token used.
     */
    @Test
    void testInvalidTokenUsed() throws Exception {
        mockMvc.perform(get("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: Standard user tries to fetch another user's profile.
     */
    @Test
    void testUserFetchingAnotherUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/8") // Standard user fetching another user
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    /**
     * TC_NEG_004: User tries to fetch an admin profile.
     */
    @Test
    void testUserFetchingAdminProfile() throws Exception {
        mockMvc.perform(get("/api/users/2") // Standard user trying to access an admin profile
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    /**
     * TC_NEG_005: Token from a deleted user is used.
     */
    // @Test
    // void testTokenFromDeletedUser() throws Exception {
    //     mockMvc.perform(get("/api/users/20") // Trying to fetch a deleted user
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(jsonPath("$.message").value("User no longer exists."));
    // }

    /**
     * TC_NEG_006: Request with an expired token.
     */
    @Test
    void testRequestWithExpiredToken() throws Exception {
        mockMvc.perform(get("/api/users/10") // Expired token
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

        mockMvc.perform(get("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User no longer exists."));
    }

    /**
     * TC_NEG_008: Request with manipulated JWT payload.
     */
    @Test
    void testManipulatedJwtPayload() throws Exception {
        String manipulatedToken = jwtUtils.generateToken("adminUser", 86400000, "ROLE_USER"); // Changed role

        mockMvc.perform(get("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + manipulatedToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }
}
