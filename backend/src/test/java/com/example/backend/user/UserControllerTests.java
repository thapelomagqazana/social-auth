package com.example.backend.user;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    private String adminToken;
    private String userToken;
    private String expiredToken;
    private String revokedToken;
    private String invalidToken = "Bearer malformed.jwt.token";
    private String manipulatedToken;
    private String untrustedToken;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final String ADMIN_ROLE = "ROLE_ADMIN";
    private final String USER_ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        // Mock the admin user
        User adminUser = new User();
        adminUser.setUsername("adminUser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("Admin@123"));  // Encode password
        adminUser.setRoles(Set.of(ADMIN_ROLE)); // Assign admin role

        User standardUser = new User();
        standardUser.setUsername("normalUser");
        standardUser.setEmail("user@example.com");
        standardUser.setPassword(passwordEncoder.encode("User@123"));
        standardUser.setRoles(Set.of(USER_ROLE));

        userRepository.save(adminUser);
        userRepository.save(standardUser);
    
        // Mock repository behavior
        Mockito.when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));
        Mockito.when(userRepository.findAll()).thenReturn(List.of(adminUser));

        Mockito.when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(standardUser));
    
        // Generate a valid JWT token for an admin
        adminToken = jwtUtils.generateToken("adminUser", 86400000, ADMIN_ROLE);

        // Generate a valid JWT token for a standard user
        userToken = jwtUtils.generateToken("normalUser", 86400000, USER_ROLE);

        // Generate an expired token
        expiredToken = jwtUtils.generateToken("adminUser",  -1000, ADMIN_ROLE);

        // Generate a revoked token (manually revoke)
        revokedToken = jwtUtils.generateToken("adminUser", 86400000, ADMIN_ROLE);
        jwtUtils.revokeToken(revokedToken);

        // Generate a manipulated token (altering payload)
        manipulatedToken = jwtUtils.generateToken("adminUser", 86400000, "ROLE_USER"); // Modified role

        // Generate a token from an untrusted source
        untrustedToken = "Bearer fake.jwt.token";
    }
    

    /**
     * TC_POS_001: Fetch all users as an admin.
     */
    @Test
    void testFetchAllUsersAsAdmin() throws Exception {
        // Mock repository returning a list of users
        List<User> users = Arrays.asList(
                new User("adminUser", "admin@example.com", "password123@!", ADMIN_ROLE),
                new User("normalUser", "user@example.com", "password123@!", USER_ROLE)
        );

        Mockito.when(userRepository.findAll()).thenReturn(users);

        // Perform GET request
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * TC_POS_002: Fetch all users when the database has multiple users.
     */
    @Test
    void testFetchAllUsersMultipleUsers() throws Exception {
        List<User> users = Arrays.asList(
                new User("user1", "user1@example.com", "password123@!", USER_ROLE),
                new User("user2", "user2@example.com", "password123@!", USER_ROLE),
                new User("user3", "user3@example.com", "password123@!", USER_ROLE)
        );

        Mockito.when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    /**
     * TC_POS_003: Fetch all users when the database is empty.
     */
    @Test
    void testFetchUsersDatabaseEmpty() throws Exception {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No users found."));
    }

    /**
     * TC_POS_004: Fetch users when user details contain special characters (e.g., username with hyphen).
     */
    @Test
    void testFetchUsersWithSpecialCharacters() throws Exception {
        List<User> users = Arrays.asList(
                new User("john-doe", "john@example.com", "password123@!", USER_ROLE),
                new User("mary_ann", "mary@example.com", "password123@!", USER_ROLE)
        );

        Mockito.when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john-doe"))
                .andExpect(jsonPath("$[1].username").value("mary_ann"));
    }

    /**
     * TC_POS_005: Fetch users when user details contain long usernames.
     */
    @Test
    void testFetchUsersWithLongUsernames() throws Exception {
        List<User> users = Arrays.asList(
                new User("thisisaverylongusernamefortestingpurposes", "longuser@example.com", "password123@!", USER_ROLE),
                new User("anotherlongusernametotestlimits", "longtest@example.com", "password123@!", USER_ROLE)
        );

        Mockito.when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("thisisaverylongusernamefortestingpurposes"))
                .andExpect(jsonPath("$[1].username").value("anotherlongusernametotestlimits"));
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid authentication token (malformed JWT).
     */
    @Test
    void testInvalidAuthToken() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: Expired token used for request.
     */
    @Test
    void testExpiredToken() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_004: Standard user tries to fetch all users.
     */
    @Test
    void testStandardUserAccessDenied() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
                // .andExpect(jsonPath("$.message").value("Access denied."));
    }

    // /**
    //  * TC_NEG_005: Token from deleted user is used.
    //  */
    // @Test
    // void testTokenFromDeletedUser() throws Exception {
    //     // Simulate user deletion
    //     Mockito.when(userRepository.findByUsername("adminUser")).thenReturn(null);

    //     mockMvc.perform(get("/api/users")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(jsonPath("$.message").value("User no longer exists."));
    // }

    /**
     * TC_NEG_006: Token from another app/system.
     */
    @Test
    void testTokenFromAnotherSystem() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, untrustedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_007: Request with manipulated JWT payload.
     */
    @Test
    void testManipulatedJwtPayload() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + manipulatedToken))
                .andExpect(status().isForbidden());
                // .andExpect(jsonPath("$.message").value("Access denied."));
    }

    // /**
    //  * TC_NEG_008: Database is unreachable.
    //  */
    // @Test
    // void testDatabaseUnreachable() throws Exception {
    //     // Simulate DB failure by making repository throw an exception
    //     Mockito.when(userRepository.findAll()).thenThrow(new RuntimeException("Database unavailable"));

    //     mockMvc.perform(get("/api/users")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Service unavailable."));
    // }

    // /**
    //  * TC_NEG_009: Redis cache is down.
    //  */
    // @Test
    // void testRedisCacheDown() throws Exception {
    //     // Simulate Redis cache failure
    //     Mockito.when(jwtUtils.isTokenRevoked(adminToken)).thenThrow(new RuntimeException("Cache unavailable"));

    //     mockMvc.perform(get("/api/users")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Cache unavailable."));
    // }

    // /**
    //  * TC_NEG_010: Admin tries with revoked token.
    //  */
    // @Test
    // void testAdminWithRevokedToken() throws Exception {
    //     mockMvc.perform(get("/api/users")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + revokedToken))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(jsonPath("$.message").value("Token is revoked."));
    // }

    /**
     * TC_CORNER_001: Request with extra query parameters
     */
    // @Test
    // void testRequestWithExtraQueryParams() throws Exception {
    //     // Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());

    //     mockMvc.perform(get("/api/users?page=1&sort=desc")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk()) // 200 OK
    //             .andExpect(jsonPath("$.message").value("No users found.")); // Should return an empty list
    // }

    /**
     * TC_CORNER_002: Request with empty Authorization header
     */
    @Test
    void testRequestWithEmptyAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // 401 Unauthorized
                .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    // /**
    //  * TC_CORNER_003: Request with an invalid JSON response format
    //  */
    // @Test
    // void testInvalidJsonResponseFormat() throws Exception {
    //     // Simulate an API returning an unexpected JSON format
    //     Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(new User()));

    //     mockMvc.perform(get("/api/users")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError()) // 500 Internal Server Error
    //             .andExpect(jsonPath("$.message").value("Unexpected response format."));
    // }

    /**
     * TC_CORNER_004: Request during high traffic
     */
    @Test
    void testRequestDuringHighTraffic() throws Exception {
        List<User> users = Arrays.asList(
                new User("adminUser", "admin@example.com", "password123@!", ADMIN_ROLE),
                new User("normalUser", "user@example.com", "password123@!", "ROLE_USER")
        );

        Mockito.when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.length()").value(2)); // Expect 2 users
    }

    /**
     * TC_CORNER_005: Request with non-existent role
     */
    @Test
    void testRequestWithNonExistentRole() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
