package com.quodbiometria.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.quodbiometria.model.entity.RefreshToken;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        // Configure properties via ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJwtThatNeedsToBeAtLeast32CharactersLong");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 86400000L);

        // Initialize the keyStore
        jwtUtil.init();

        // Use lenient() to avoid "unnecessary stubbing" errors
        lenient().when(userDetails.getUsername()).thenReturn("user@example.com");
        lenient().when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtUtil.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testValidateToken_Valid() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act & Assert
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_InvalidUsername() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);
        User differentUser = new User();
        differentUser.setEmail("different@example.com");

        // Act & Assert
        assertFalse(jwtUtil.validateToken(token, differentUser));
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 1L);
        String token = jwtUtil.generateToken(userDetails);

        Thread.sleep(10);

        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L);

        assertFalse(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.extractUsername(token);

        assertEquals("user@example.com", username);
    }

    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(userDetails);

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testGenerateRefreshToken() {
        // Arrange
        User user = new User();
        user.setId("user123");
        user.setEmail("user@example.com");

        // Act
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
    }
}
