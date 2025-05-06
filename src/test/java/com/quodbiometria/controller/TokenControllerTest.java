package com.quodbiometria.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quodbiometria.config.JwtAuthenticationFilter;
import com.quodbiometria.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodbiometria.model.dto.request.RefreshTokenRequestDTO;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.service.RefreshTokenService;

@WebMvcTest(TokenController.class)
@AutoConfigureMockMvc(addFilters = false) // Desabilita os filtros de segurança para o teste
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthFilter;

    @MockBean
    private RefreshTokenService refreshTokenService;

    private RefreshTokenRequestDTO refreshTokenRequest;
    private AuthenticationResponseDTO authResponse;

    @BeforeEach
    void setUp() {
        refreshTokenRequest = new RefreshTokenRequestDTO();
        refreshTokenRequest.setRefreshToken("test.refresh.token");

        authResponse = AuthenticationResponseDTO.builder()
                .accessToken("new.access.token")
                .refreshToken("new.refresh.token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }

    @Test
    void testRefreshToken_ValidToken_ReturnsNewTokens() throws Exception {
        when(refreshTokenService.refreshToken(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token atualizado com sucesso"))
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new.refresh.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }
}
