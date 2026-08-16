package com.hardwarehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real API contract end-to-end (Spring context, real
 * DataSeeder, real database) rather than mocking anything, unlike
 * HardwareServiceTest. Uses a separate test database (application-test.properties)
 * so this never touches the dev database.
 *
 * Logs in through the real /auth/login endpoint rather than issuing a token
 * directly, since Block G made every hardware endpoint require
 * authentication — this exercises the actual login flow too, not just a
 * shortcut around it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HardwareControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void logInAsAdmin() throws Exception {
        // test-admin@example.com is defined in application-test.properties
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test-admin@example.com\",\"password\":\"test-only-not-a-real-password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void getReturnsTheSeededList() throws Exception {
        mockMvc.perform(get("/api/hardware").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11));
    }

    @Test
    void rentingAnUnavailableDeviceReturns409WithTheExpectedErrorBody() throws Exception {
        // seed id 2 (Apple MacBook Pro 13) is loaded as IN_USE
        mockMvc.perform(post("/api/hardware/2/rent")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userEmail\":\"test@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
