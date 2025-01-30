package com.example.backend.user;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.example.backend.security.UserDetailsServiceImpl;

import org.junit.jupiter.api.AfterEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for PUT /api/users/{id} - Update a specific user's profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UpdateUserControllerTests {

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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final String ADMIN_ROLE = "ADMIN";
    private final String USER_ROLE = "USER";
    
    @BeforeEach
    void setUp() {
        // Mock Admin User
        User adminUser = new User(1L, "adminUser", "admin@example.com", passwordEncoder.encode("Admin@123"), Set.of(ADMIN_ROLE));
        // Mock Standard User
        User normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("User@123"), Set.of(USER_ROLE));

        // Generate JWT Tokens
        adminToken = jwtUtils.generateToken(adminUser.getUsername(), 86400000, ADMIN_ROLE);
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, USER_ROLE);

        // Mock Repository
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
        Mockito.when(userRepository.findById(7L)).thenReturn(Optional.of(new User(7L, "existingUser", "existing@example.com", "password", Set.of(USER_ROLE))));
        Mockito.when(userRepository.findById(9L)).thenReturn(Optional.of(new User(9L, "userToPromote", "promote@example.com", "password", Set.of(USER_ROLE))));
        Mockito.when(userRepository.findById(12L)).thenReturn(Optional.of(new User(12L, "specialUser", "special@example.com", "password", Set.of(USER_ROLE))));
        Mockito.when(userRepository.findById(8L)).thenReturn(Optional.of(new User(8L, "optionalUser", "optional@example.com", "password", Set.of(USER_ROLE))));

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
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * TC_POS_001: Admin updates a user's profile successfully.
     */
    @Test
    void testAdminUpdatesUserProfile() throws Exception {
        String requestBody = """
            {
                "username": "updatedUser",
                "email": "updated@example.com"
            }
        """;

        mockMvc.perform(put("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("updatedUser"))
                .andExpect(jsonPath("$.user.email").value("updated@example.com"));
    }

    /**
     * TC_POS_002: User updates their own profile successfully.
     */
    @Test
    void testUserUpdatesOwnProfile() throws Exception {
        String requestBody = """
            {
                "username": "newUsername",
                "email": "newuser@example.com"
            }
        """;

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("newUsername"))
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"));
    }

    /**
     * TC_POS_003: Admin updates a user’s email and username.
     */
    @Test
    void testAdminUpdatesUserEmailAndUsername() throws Exception {
        String requestBody = """
            {
                "username": "updatedUsername",
                "email": "updated@example.com"
            }
        """;

        mockMvc.perform(put("/api/users/7")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("updatedUsername"))
                .andExpect(jsonPath("$.user.email").value("updated@example.com"));
    }

    /**
     * TC_POS_004: Admin updates a user's role from USER to ADMIN.
     */
    @Test
    void testAdminUpdatesUserRole() throws Exception {
        String requestBody = """
            {
                "roles": ["ADMIN"]
            }
        """;

        mockMvc.perform(put("/api/users/9")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.roles[0]").value("ADMIN"));
    }

    /**
     * TC_POS_005: User updates their password only.
     */
    @Test
    void testUserUpdatesPassword() throws Exception {
        String requestBody = """
            {
                "password": "NewStrongP@ssw0rd"
            }
        """;

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile updated successfully"));
    }

    /**
     * TC_POS_006: Updating a profile with special characters in username.
     */
    @Test
    void testUpdatingProfileWithSpecialCharactersInUsername() throws Exception {
        String requestBody = """
            {
                "username": "john_doe@2024!"
            }
        """;

        mockMvc.perform(put("/api/users/12")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("john_doe@2024!"));
    }

    // /**
    //  * TC_POS_007: Admin updates a profile with valid but optional fields.
    //  */
    // @Test
    // void testAdminUpdatesUserWithOptionalFields() throws Exception {
    //     String requestBody = """
    //         {
    //             "phoneNumber": "123-456-7890"
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/8")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.user.phoneNumber").value("123-456-7890"));
    // }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthTokenProvided() throws Exception {
        String requestBody = """
            {
                "username": "unauthorizedUpdate"
            }
        """;

        mockMvc.perform(put("/api/users/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid authentication token used.
     */
    @Test
    void testInvalidTokenUsed() throws Exception {
        String requestBody = """
            {
                "username": "invalidTokenUser"
            }
        """;

        mockMvc.perform(put("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer InvalidToken123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: Standard user tries to update another user's profile.
     */
    @Test
    void testUserUpdatingAnotherUserProfile() throws Exception {
        String requestBody = """
            {
                "username": "unauthorizedUpdate"
            }
        """;

        mockMvc.perform(put("/api/users/8")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    /**
     * TC_NEG_004: Standard user tries to update an admin’s profile.
     */
    @Test
    void testUserUpdatingAdminProfile() throws Exception {
        String requestBody = """
            {
                "username": "unauthorizedUpdate"
            }
        """;

        mockMvc.perform(put("/api/users/2")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    // /**
    //  * TC_NEG_005: Token from a deleted user is used.
    //  */
    // @Test
    // void testTokenFromDeletedUser() throws Exception {
    //     String requestBody = """
    //         {
    //             "username": "deletedUser"
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/20")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(jsonPath("$.message").value("User no longer exists"));
    // }

    /**
     * TC_NEG_006: Expired token is used.
     */
    @Test
    void testExpiredToken() throws Exception {
        String requestBody = """
            {
                "username": "expiredTokenUser"
            }
        """;

        mockMvc.perform(put("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "expiredToken===!$")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_007: JWT token from another system/app used.
     */
    // @Test
    // void testTokenFromAnotherSystem() throws Exception {
    //     String foreignToken = jwtUtils.generateToken("hacker", 86400000, "UNKNOWN_ROLE");

    //     String requestBody = """
    //         {
    //             "username": "untrustedTokenUser"
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/10")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(jsonPath("$.message").value("Invalid token"));
    // }

    // /**
    //  * TC_NEG_008: Request with manipulated JWT payload.
    //  */
    // @Test
    // void testManipulatedJwtPayload() throws Exception {
    //     String manipulatedToken = jwtUtils.generateToken("adminUser", 86400000, "USER"); // Changed role

    //     String requestBody = """
    //         {
    //             "username": "manipulatedPayload"
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/10")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + manipulatedToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isForbidden())
    //             .andExpect(jsonPath("$.message").value("Access denied"));
    // }

    // /**
    //  * TC_NEG_009: Updating email to an already used email.
    //  */
    // @Test
    // void testUpdatingEmailToExistingOne() throws Exception {
    //     User user = new User(7L, "existingUser", "existing@example.com", "password", Set.of(USER_ROLE));

    //     userRepository.save(user);
    //     String requestBody = """
    //         {
    //             "email": "existing@example.com"
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/5")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.message").value("Email is already in use"));
    // }

    // /**
    //  * TC_NEG_010: Updating username to an already used one.
    //  */
    // @Test
    // void testUpdatingUsernameToExistingOne() throws Exception {
    //     String requestBody = """
    //         {
    //             "username": "existingUser"
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/5")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.message").value("Username is already taken"));
    // }

    /**
     * TC_EDGE_002: Updating email with invalid format.
     */
    @Test
    void testUpdatingEmailWithInvalidFormat() throws Exception {
        String requestBody = """
            {
                "email": "invalid-email"
            }
        """;

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format"));
    }

    /**
     * TC_EDGE_003: Updating password with weak strength.
     */
    @Test
    void testUpdatingPasswordWithWeakStrength() throws Exception {
        String requestBody = """
            {
                "password": "123"
            }
        """;

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password is too weak"));
    }

    /**
     * TC_EDGE_004: Updating roles as a standard user.
     */
    // @Test
    // void testUpdatingRolesAsStandardUser() throws Exception {
    //     String requestBody = """
    //         {
    //             "roles": ["ADMIN"]
    //         }
    //     """;

    //     mockMvc.perform(put("/api/users/5")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken) // Standard user
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(requestBody))
    //             .andExpect(status().isForbidden())
    //             .andExpect(jsonPath("$.message").value("Access denied"));
    // }

    /**
     * TC_EDGE_005: Updating username to an empty string.
     */
    @Test
    void testUpdatingUsernameToEmptyString() throws Exception {
        String requestBody = """
            {
                "username": ""
            }
        """;

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username cannot be empty"));
    }

    /**
     * TC_EDGE_006: Updating all fields with excessive lengths.
     */
    @Test
    void testUpdatingFieldsWithExcessiveLengths() throws Exception {
        String longString = "A".repeat(500); // 500-character long string

        String requestBody = """
            {
                "username": "%s",
                "email": "%s@example.com",
                "password": "%s"
            }
        """.formatted(longString, longString, longString);

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Field length exceeds the limit"));
    }
}
