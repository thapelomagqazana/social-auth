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
 * Tests for GET /api/users/me - Fetch the currently logged-in user's info.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GetCurrentUserControllerTests {

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
    private String multiRoleToken;
    private String specialUserToken;

    private String validUserToken;
    private String expiredUserToken;
    private String invalidToken = "InvalidJWTTokenString";
    private String foreignSystemToken;
    private String deletedUserToken;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final String ADMIN_ROLE = "ADMIN";
    private final String USER_ROLE = "USER";

    @BeforeEach
    void setUp() {
        // Mock Standard User
        User normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("User@123"), Set.of(USER_ROLE));

        new User(5L, "normalUser1", "user@example.com", passwordEncoder.encode("User@123"), Set.of(USER_ROLE));

        // Mock Admin User
        User adminUser = new User(1L, "adminUser", "admin@example.com", passwordEncoder.encode("Admin@123"), Set.of(ADMIN_ROLE));

        // Mock User with Multiple Roles
        User multiRoleUser = new User(7L, "multiRoleUser", "multi@example.com", passwordEncoder.encode("Password@123"), Set.of(USER_ROLE, ADMIN_ROLE));

        // Mock User with Special Characters in Username
        User specialCharUser = new User(15L, "john_doe@2024!", "special@example.com", passwordEncoder.encode("Password@123"), Set.of(USER_ROLE));

        // Generate JWT Tokens
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, USER_ROLE);
        adminToken = jwtUtils.generateToken(adminUser.getUsername(), 86400000, ADMIN_ROLE);
        multiRoleToken = jwtUtils.generateToken(multiRoleUser.getUsername(), 86400000, USER_ROLE + "," + ADMIN_ROLE);
        specialUserToken = jwtUtils.generateToken(specialCharUser.getUsername(), 86400000, USER_ROLE);

        // Generate JWT Tokens
        validUserToken = jwtUtils.generateToken("normalUser1", 86400000, "USER");
        expiredUserToken = jwtUtils.generateToken("expiredUser", -1, "USER"); // Expired Token
        foreignSystemToken = jwtUtils.generateToken("hacker", 86400000, "UNKNOWN");
        deletedUserToken = jwtUtils.generateToken("deletedUser", 86400000, "ADMIN");

        // Mock Repository Responses
        Mockito.when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(normalUser));
        Mockito.when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findByUsername("multiRoleUser")).thenReturn(Optional.of(multiRoleUser));
        Mockito.when(userRepository.findByUsername("john_doe@2024!")).thenReturn(Optional.of(specialCharUser));

        // Mock User Repository
        Mockito.when(userRepository.findByUsername("normalUser1")).thenReturn(Optional.empty()); // Simulate user deletion
        Mockito.when(userRepository.findByUsername("deletedUser")).thenReturn(Optional.empty()); // Simulate deleted user

        // Mock UserDetailsService
        Mockito.when(userDetailsService.loadUserByUsername("normalUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("normalUser")
                .password(normalUser.getPassword())
                .roles(USER_ROLE)
                .build());

        Mockito.when(userDetailsService.loadUserByUsername("adminUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("adminUser")
                .password(adminUser.getPassword())
                .roles(ADMIN_ROLE)
                .build());

        Mockito.when(userDetailsService.loadUserByUsername("multiRoleUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("multiRoleUser")
                .password(multiRoleUser.getPassword())
                .roles(USER_ROLE, ADMIN_ROLE)
                .build());

        Mockito.when(userDetailsService.loadUserByUsername("john_doe@2024!"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("john_doe@2024!")
                .password(specialCharUser.getPassword())
                .roles(USER_ROLE)
                .build());
    }

    /**
     * TC_POS_001: Fetch own profile as a standard user.
     */
    @Test
    void testFetchOwnProfileAsUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("normalUser"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    /**
     * TC_POS_002: Fetch own profile as an admin.
     */
    @Test
    void testFetchOwnProfileAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminUser"))
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    /**
     * TC_POS_003: Fetch profile where the user has multiple roles.
     */
    @Test
    void testFetchUserWithMultipleRoles() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + multiRoleToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("multiRoleUser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2));
    }

    /**
     * TC_POS_004: Fetch profile with special characters in the username.
     */
    @Test
    void testFetchUserWithSpecialCharactersInUsername() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + specialUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe@2024!"))
                .andExpect(jsonPath("$.email").value("special@example.com"));
    }

    /**
     * TC_POS_005: Fetch profile immediately after login.
     */
    @Test
    void testFetchProfileImmediatelyAfterLogin() throws Exception {
        // Simulate a login request
        String tokenAfterLogin = jwtUtils.generateToken("normalUser", 86400000, USER_ROLE);

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAfterLogin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("normalUser"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users/me") // No Authorization header
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }


    /**
     * TC_NEG_002: Invalid authentication token used.
     */
    @Test
    void testInvalidTokenUsed() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: Token from a deleted user is used.
     */
    @Test
    void testTokenFromDeletedUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deletedUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User no longer exists."));
    }

    /**
     * TC_NEG_004: Expired token is used.
     */
    @Test
    void testRequestWithExpiredToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_005: JWT token from another system/app used.
     */
    @Test
    void testTokenFromAnotherSystem() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignSystemToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User no longer exists."));
    }

    // /**
    //  * TC_NEG_006: Request with manipulated JWT payload.
    //  */
    // @Test
    // void testManipulatedJwtPayload() throws Exception {
    //     String manipulatedToken = jwtUtils.generateToken("normalUser", 86400000, "ROLE_UNKNOWN"); // Changed role

    //     mockMvc.perform(get("/api/users/me")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + manipulatedToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isForbidden())
    //             .andExpect(jsonPath("$.message").value("Access denied."));
    // }

    // /**
    //  * TC_NEG_007: User exists in JWT but not in DB.
    //  */
    // @Test
    // void testUserExistsInJwtButNotInDB() throws Exception {
    //     mockMvc.perform(get("/api/users/me")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + validUserToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isNotFound())
    //             .andExpect(jsonPath("$.message").value("User not found."));
    // }
}
