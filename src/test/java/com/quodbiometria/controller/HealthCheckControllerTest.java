package com.quodbiometria.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.quodbiometria.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthCheckController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testHealthCheck_ReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/api/health").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testApplicationInfo_ReturnsAppInfo() throws Exception {
        mockMvc.perform(get("/api/info").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.environment").exists())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.system.java").exists())
                .andExpect(jsonPath("$.system.javaVendor").exists())
                .andExpect(jsonPath("$.system.os").exists())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.memory.freeMemory").exists())
                .andExpect(jsonPath("$.memory.totalMemory").exists())
                .andExpect(jsonPath("$.memory.maxMemory").exists());
    }
}
