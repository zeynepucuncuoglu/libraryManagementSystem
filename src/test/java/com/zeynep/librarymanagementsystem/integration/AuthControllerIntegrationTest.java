package com.zeynep.librarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.model.Role;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthControllerIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll(); // clean H2 between tests
    }

    @Test
    void shouldRegisterUserSuccessfully_whenValidInputProvided() throws Exception {
        logger.info("Starting Test: shouldRegisterUserSuccessfully_whenValidInputProvided");
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Jane Doe");
        user.put("email", "jane.doe@example.com");
        user.put("role", "PATRON");
        user.put("contactInfo", "+1-234-567-8901");
        user.put("password", "StrongPass123");

        logger.debug("User data for registration: {}", user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));

        logger.info("Completed Test: shouldRegisterUserSuccessfully_whenValidInputProvided");

    }

    @Test
    void shouldReturnToken_whenLoginWithCorrectCredentials() throws Exception {
        logger.info("Starting Test: shouldReturnToken_whenLoginWithCorrectCredentials");
        // manually save a user with encoded password for login test
        User user = User.builder()
                .name("Jane Doe")
                .email("jane.login@example.com")
                .password(passwordEncoder.encode("LoginPass123"))
                .role(Role.PATRON)
                .contactInfo("123456789")
                .build();
        userRepository.save(user);

        Map<String, String> loginPayload = Map.of(
                "email", "jane.login@example.com",
                "password", "LoginPass123"
        );

        logger.debug("Login payload: {}", loginPayload);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        logger.info("Completed Test: shouldReturnToken_whenLoginWithCorrectCredentials");
    }

    @Test
    void shouldFailRegistration_whenEmailIsInvalid() throws Exception {
        logger.info("Starting Test: shouldFailRegistration_whenEmailIsInvalid");
        UserDTO invalidDto = new UserDTO(
                null,
                "Bad Email",
                "not-an-email",
                Role.PATRON,
                null,
                "StrongPass123"
        );

        logger.debug("Invalid registration data: {}", invalidDto);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        logger.info("Completed Test: shouldFailRegistration_whenEmailIsInvalid");
    }

    @Test
    void shouldFailLogin_whenPasswordIsIncorrect() throws Exception {
        logger.info("Starting Test: shouldFailLogin_whenPasswordIsIncorrect");
        User user = User.builder()
                .name("Wrong Password")
                .email("wrong.pass@example.com")
                .password(passwordEncoder.encode("Correct123"))
                .role(Role.PATRON)
                .build();
        userRepository.save(user);

        Map<String, String> loginPayload = Map.of(
                "email", "wrong.pass@example.com",
                "password", "Wrong123"
        );

        logger.debug("Login payload for incorrect password: {}", loginPayload);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailLogin_whenPasswordIsIncorrect");
    }
}
