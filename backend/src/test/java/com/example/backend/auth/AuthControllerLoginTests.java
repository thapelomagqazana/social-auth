package com.example.backend.auth;

import com.example.backend.dto.LoginRequest;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerLoginTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        userRepository.deleteAll();

        // Prepopulate the database with test users
        User validUser = new User();
        validUser.setUsername("validuser");
        validUser.setPassword(passwordEncoder.encode("Password@123"));
        validUser.setEmail("validuser@example.com");
        validUser.getRoles().add("USER");
        userRepository.save(validUser);

        User secureUser = new User();
        secureUser.setUsername("secureuser");
        secureUser.setPassword(passwordEncoder.encode("StrongPassword@2023"));
        secureUser.setEmail("secureuser@example.com");
        secureUser.getRoles().add("USER");
        userRepository.save(secureUser);

        User specialCharUser = new User();
        specialCharUser.setUsername("user_special!@#");
        specialCharUser.setPassword(passwordEncoder.encode("Password@123"));
        specialCharUser.setEmail("user_special@example.com");
        specialCharUser.getRoles().add("USER");
        userRepository.save(specialCharUser);
    }

    @Test
    @DisplayName("TC_POS_001: Valid username and password")
    void testLoginWithValidUsernameAndPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "validuser",
                          "password": "Password@123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()) // Ensure token is returned
                .andExpect(jsonPath("$").isNotEmpty()); // Ensure token is not empty
    }

    @Test
    @DisplayName("TC_POS_002: Case-insensitive username")
    void testLoginWithCaseInsensitiveUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "VALIDUSER",
                          "password": "Password@123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("TC_POS_003: Valid user with a strong password")
    void testLoginWithStrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "secureuser",
                          "password": "StrongPassword@2023"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("TC_POS_004: Username with special characters")
    void testLoginWithSpecialCharactersInUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "user_special!@#",
                          "password": "Password@123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("TC_NEG_001: Missing username field")
    void testMissingUsernameField() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username is required."));
    }

    @Test
    @DisplayName("TC_NEG_002: Missing password field")
    void testMissingPasswordField() throws Exception {
        String payload = """
                {
                  "username": "validuser"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required."));
    }

    @Test
    @DisplayName("TC_NEG_003: Invalid username")
    void testInvalidUsername() throws Exception {
        String payload = """
                {
                  "username": "nonexistentuser",
                  "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("TC_NEG_004: Invalid password")
    void testInvalidPassword() throws Exception {
        String payload = """
                {
                  "username": "validuser",
                  "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("TC_NEG_005: Missing both username and password")
    void testMissingBothUsernameAndPassword() throws Exception {
        String payload = """
                {}
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("TC_NEG_006: SQL injection in username")
    void testSqlInjectionInUsername() throws Exception {
        String payload = """
                {
                  "username": "' OR 1=1; --",
                  "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("TC_NEG_007: SQL injection in password")
    void testSqlInjectionInPassword() throws Exception {
        String payload = """
                {
                  "username": "validuser",
                  "password": "' OR 1=1; --"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("TC_NEG_008: Password does not meet complexity")
    void testPasswordDoesNotMeetComplexity() throws Exception {
        String payload = """
                {
                  "username": "validuser",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("TC_NEG_009: Disabled account login attempt")
    void testDisabledAccountLoginAttempt() throws Exception {
        // Create a disabled user
        User disabledUser = new User();
        disabledUser.setUsername("disableduser");
        disabledUser.setEmail("disableduser@example.com");
        disabledUser.setPassword(passwordEncoder.encode("Password@123"));
        disabledUser.setEnabled(false); // Set account as disabled
        userRepository.save(disabledUser);

        // Prepare login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("disableduser");
        loginRequest.setPassword("Password@123");

        // Perform login attempt
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden()) // Expect 403 Forbidden
                .andExpect(jsonPath("$.message").value("Account is disabled."));
    }

    @Test
    @DisplayName("TC_CORNER_001: Extra fields in the payload")
    void testExtraFieldsInPayload() throws Exception {
        String payload = """
            {
              "username": "validuser",
              "password": "Password@123",
              "extraField": "extra"
            }
        """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("TC_CORNER_002: Empty JSON payload")
    void testEmptyJsonPayload() throws Exception {
        String payload = "{}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("TC_CORNER_003: Invalid JSON format")
    void testInvalidJsonFormat() throws Exception {
        String payload = """
            { "username": "validuser", "password": "Password@123"
        """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

//     @Test
//     @DisplayName("TC_CORNER_004: Large payload size")
//     void testLargePayloadSize() throws Exception {
//         StringBuilder largePayload = new StringBuilder();
//         largePayload.append("{ \"username\": \"validuser\", \"password\": \"Password@123\"");
//         for (int i = 0; i < 100000; i++) {
//             largePayload.append(", \"extraField").append(i).append("\": \"extraValue").append(i).append("\"");
//         }
//         largePayload.append(" }");
    
//         mockMvc.perform(post("/api/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(largePayload.toString()))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Payload size exceeds the allowed limit."));
//     }
    

    @Test
    @DisplayName("TC_CORNER_005: Case-sensitive username and password")
    void testCaseSensitiveUsernameAndPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("VALIDuser");
        loginRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()); // Adjust based on case-sensitivity rules
    }

    // @Test
    // @DisplayName("TC_CORNER_006: Login during database downtime")
    // void testLoginDuringDatabaseDowntime() throws Exception {
    //     userRepository.deleteAll(); // Simulating database downtime

    //     LoginRequest loginRequest = new LoginRequest();
    //     loginRequest.setUsername("validuser");
    //     loginRequest.setPassword("Password@123");

    //     mockMvc.perform(post("/api/auth/login")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(loginRequest)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Database unavailable."));
    // }

}
