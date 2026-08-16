package com.hardwarehub.controller;

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
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HardwareControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReturnsTheSeededList() throws Exception {
        mockMvc.perform(get("/api/hardware"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11));
    }

    @Test
    void rentingAnUnavailableDeviceReturns409WithTheExpectedErrorBody() throws Exception {
        // seed id 2 (Apple MacBook Pro 13) is loaded as IN_USE
        mockMvc.perform(post("/api/hardware/2/rent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userEmail\":\"test@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
