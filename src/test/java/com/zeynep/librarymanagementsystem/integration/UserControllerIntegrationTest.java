package com.zeynep.librarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.model.Role;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
class UserControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String librarianToken;
    private User librarianUser;

    private User patronUser;
    private  String patronToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        librarianUser = new User();
        librarianUser.setName("Librarian");
        librarianUser.setEmail("librarian@example.com");
        librarianUser.setPassword("Password124");
        librarianUser.setRole(Role.LIBRARIAN);

        librarianUser = userRepository.save(librarianUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(librarianUser.getEmail())
                .password(librarianUser.getPassword())
                .roles(librarianUser.getRole().name())
                .build();

        librarianToken = jwtService.generateToken(userDetails);

        patronUser = new User();
        patronUser.setName("Patron");
        patronUser.setEmail("patron@example.com");
        patronUser.setPassword("Password124");
        patronUser.setRole(Role.PATRON);

        patronUser = userRepository.save(patronUser);

        UserDetails patronUserDetails  = org.springframework.security.core.userdetails.User
                .withUsername(patronUser.getEmail())
                .password(patronUser.getPassword())
                .roles(patronUser.getRole().name())
                .build();


        patronToken = jwtService.generateToken(patronUserDetails);
    }

    @Test
    void shouldGetUserById_whenValidRequest() throws Exception {
        logger.info("Starting Test: shouldGetUserById_whenValidRequest");
        mockMvc.perform(get("/api/users/" + librarianUser.getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(librarianUser.getEmail()));
        logger.info("Completed Test: shouldGetUserById_whenValidRequest");
    }

    @Test
    void shouldFailToGetUserById_whenUserIsNotLibrarian() throws Exception {
        logger.info("Starting Test: shouldGetUserById_whenUserIsNotLibrarian");
        mockMvc.perform(get("/api/users/" + librarianUser.getId())
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
        logger.info("Completed Test: shouldGetUserById_whenUserIsNotLibrarian");

    }

    @Test
    void shouldFailToGetUserById_whenAuthorizationTokenIsMissing() throws Exception {

        logger.info("Starting Test: shouldGetUserById_whenAuthorizationTokenIsMissing");
        mockMvc.perform(get("/api/users/" + librarianUser.getId()))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldGetUserById_whenAuthorizationTokenIsMissing");

    }

    @Test
    void shouldFailGetUserById_whenUserNotFound() throws Exception {
        logger.info("Starting Test: shouldGetUserById_whenUserNotFound");
        Long invalidLongId = 999999L;
        mockMvc.perform(get("/api/users/" + invalidLongId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound());
        logger.info("Completed Test: shouldGetUserById_whenUserNotFound");
    }

    @Test
    void shouldGetAllUsers_whenUserIsLibrarian() throws Exception {
        logger.info("Starting Test: shouldGetAllUsers_whenUserIsLibrarian");
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(librarianUser.getEmail()));
        logger.info("Completed Test: shouldGetAllUsers_whenUserIsLibrarian");
    }

    @Test
    void shouldGetAllUsers_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldGetAllUsers_whenAuthorizationTokenIsMissing");
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldGetAllUsers_whenAuthorizationTokenIsMissing");

    }

    @Test
    void shouldGetAllUsers_whenUserIsNotLibrarian() throws Exception {
        logger.info("Starting Test: shouldGetAllUsers_whenUserIsNotLibrarian");
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
        logger.info("Completed Test: shouldGetAllUsers_whenUserIsNotLibrarian");

    }




    @Test
    void shouldUpdateUser_whenUserIsLibrarianAndValidRequest() throws Exception {
        logger.info("Starting Test: shouldUpdateUser_whenUserIsLibrarianAndValidRequest");
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Updated Librarian");
        payload.put("email", librarianUser.getEmail());
        payload.put("password", "NewValidPassword123"); // Password for update
        payload.put("role", "LIBRARIAN");
        payload.put("contactInfo", "+1-234-567-8901");

        mockMvc.perform(put("/api/users/" + librarianUser.getId())
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Librarian"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Ensure password is not in response
        logger.info("Completed Test: shouldUpdateUser_whenUserIsLibrarianAndValidRequest");
    }


    @Test
    void shouldFailUpdateUser_whenInvalidData() throws Exception {
        logger.info("Starting Test: shouldUpdateUser_whenInvalidData");
        UserDTO updateDto = new UserDTO();
        updateDto.setName(""); // Invalid name
        updateDto.setEmail(librarianUser.getEmail());
        updateDto.setRole(Role.LIBRARIAN);

        mockMvc.perform(put("/api/users/" + librarianUser.getId())
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
        logger.info("Completed Test: shouldUpdateUser_whenInvalidData");
    }

    @Test
    void shouldFailUpdateUser_whenUserNotFound() throws Exception {
        logger.info("Starting Test: shouldUpdateUser_whenUserNotFound");
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Does Not Exist");
        payload.put("email", librarianUser.getEmail());
        payload.put("password", "NewValidPassword123"); // Password for update
        payload.put("role", "LIBRARIAN");
        payload.put("contactInfo", "+1-234-567-8901");

        mockMvc.perform(put("/api/users/99999")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
        logger.info("Completed Test: shouldUpdateUser_whenUserNotFound.");
    }




    @Test
    void shouldDeleteUser_whenValidRequest() throws Exception {
        logger.info("Starting Test: shouldDeleteUser_whenValidRequest");
        mockMvc.perform(delete("/api/users/" + patronUser.getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNoContent());
        logger.info("Completed Test: shouldDeleteUser_whenValidRequest");
    }

    @Test
    void shouldFailDeleteUser_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldFailDeleteUser_whenAuthorizationTokenIsMissing");
        mockMvc.perform(delete("/api/users/" + patronUser.getId()))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailDeleteUser_whenAuthorizationTokenIsMissing");
    }

    @Test
    void shouldFailDeleteUser_whenUserIsNotLibrarian() throws Exception {

        logger.info("Starting Test: shouldFailDeleteUser_whenUserIsNotLibrarian");
        mockMvc.perform(delete("/api/users/" + patronUser.getId())
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
        logger.info("Completed Test: shouldFailDeleteUser_whenUserIsNotLibrarian.");
    }

    @Test
    void shouldFailDeleteUser_whenUserNotFound() throws Exception {
        logger.info("Starting Test: shouldFailDeleteUser_whenUserNotFound");
        Long invalidLongId = 999999L;

        mockMvc.perform(delete("/api/users/" + invalidLongId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound());
        logger.info("Completed Test: shouldFailDeleteUser_whenUserNotFound.");
    }

}
