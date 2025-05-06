package com.quodbiometria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quodbiometria.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodbiometria.model.dto.request.AuthenticationRequestDTO;
import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    private AuthenticationRequestDTO authRequest;
    private UserCreateDTO registerRequest;
    private AuthenticationResponseDTO authResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthenticationRequestDTO();
        authRequest.setEmail("user@example.com");
        authRequest.setPassword("password123");

        registerRequest = new UserCreateDTO();
        registerRequest.setName("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");

        authResponse = AuthenticationResponseDTO.builder()
                .accessToken("test.access.token")
                .refreshToken("test.refresh.token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }

    @Test
    void testAuthenticate_ValidCredentials_ReturnsToken() throws Exception {
        when(authService.authenticate(any(AuthenticationRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.data.accessToken").value("test.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }

    @Test
    void testRegister_ValidData_ReturnsToken() throws Exception {
        when(authService.register(any(UserCreateDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.accessToken").value("test.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }
}
