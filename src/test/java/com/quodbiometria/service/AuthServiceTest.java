package com.quodbiometria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.quodbiometria.exception.RateLimitExceededException;
import com.quodbiometria.model.dto.request.AuthenticationRequestDTO;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    private AuthenticationRequestDTO validRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600000L);

        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        validRequest = new AuthenticationRequestDTO();
        validRequest.setEmail("user@example.com");
        validRequest.setPassword("password123");

        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("user@example.com");
        testUser.setPassword("hashedPassword");
    }

    @Test
    void testAuthenticate_ValidCredentials_Success() {
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.loadUserByUsername("user@example.com")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("access.token.value");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refresh.token.value");

        AuthenticationResponseDTO response = authService.authenticate(validRequest);

        assertNotNull(response);
        assertEquals("access.token.value", response.getAccessToken());
        assertEquals("refresh.token.value", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600, response.getExpiresIn());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(validRequest.getEmail(), validRequest.getPassword()));
    }

    @Test
    void testAuthenticate_RateLimitExceeded_ThrowsException() {
        when(rateLimitService.tryConsume(anyString())).thenReturn(false);

        assertThrows(RateLimitExceededException.class, () -> authService.authenticate(validRequest));
    }

    @Test
    void testAuthenticate_InvalidCredentials_ThrowsException() {
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(validRequest));
    }
}
