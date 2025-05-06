package com.quodbiometria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.quodbiometria.exception.TokenRefreshException;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.model.entity.RefreshToken;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.repository.RefreshTokenRepository;
import com.quodbiometria.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    private RefreshToken validRefreshToken;
    private RefreshToken expiredRefreshToken;
    private RefreshToken revokedRefreshToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "accessTokenExpiration", 3600000L);

        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("user@example.com");

        validRefreshToken = new RefreshToken();
        validRefreshToken.setId("token1");
        validRefreshToken.setToken("valid-refresh-token");
        validRefreshToken.setUserId("user123");
        validRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        validRefreshToken.setRevoked(false);

        expiredRefreshToken = new RefreshToken();
        expiredRefreshToken.setId("token2");
        expiredRefreshToken.setToken("expired-refresh-token");
        expiredRefreshToken.setUserId("user123");
        expiredRefreshToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        expiredRefreshToken.setRevoked(false);

        revokedRefreshToken = new RefreshToken();
        revokedRefreshToken.setId("token3");
        revokedRefreshToken.setToken("revoked-refresh-token");
        revokedRefreshToken.setUserId("user123");
        revokedRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        revokedRefreshToken.setRevoked(true);
    }

    @Test
    void testRefreshToken_ValidToken_ReturnsNewTokens() {
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));
        when(userService.loadUserById("user123")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validRefreshToken);

        AuthenticationResponseDTO response = refreshTokenService.refreshToken("valid-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600, response.getExpiresIn());

        verify(refreshTokenRepository).save(argThat(RefreshToken::isRevoked));
    }

    @Test
    void testRefreshToken_TokenNotFound_ThrowsException() {
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.refreshToken("invalid-token"));

        assertEquals("Refresh token não encontrado", exception.getMessage());
    }

    @Test
    void testRefreshToken_ExpiredToken_ThrowsException() {
        when(refreshTokenRepository.findByToken("expired-refresh-token")).thenReturn(Optional.of(expiredRefreshToken));

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.refreshToken("expired-refresh-token"));

        assertEquals("Refresh token expirado ou revogado", exception.getMessage());
        verify(refreshTokenRepository).delete(expiredRefreshToken);
    }

    @Test
    void testRefreshToken_RevokedToken_ThrowsException() {
        when(refreshTokenRepository.findByToken("revoked-refresh-token")).thenReturn(Optional.of(revokedRefreshToken));

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.refreshToken("revoked-refresh-token"));

        assertEquals("Refresh token expirado ou revogado", exception.getMessage());
        verify(refreshTokenRepository).delete(revokedRefreshToken);
    }

    @Test
    void testRevokeAllUserTokens_Success() {
        RefreshToken token1 = new RefreshToken();
        token1.setRevoked(false);

        RefreshToken token2 = new RefreshToken();
        token2.setRevoked(false);

        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse("user123"))
                .thenReturn(Arrays.asList(token1, token2));

        refreshTokenService.revokeAllUserTokens("user123");

        verify(refreshTokenRepository, times(2)).save(argThat(RefreshToken::isRevoked));
    }

    @Test
    void testDeleteByUserId_Success() {
        refreshTokenService.deleteByUserId("user123");

        verify(refreshTokenRepository).deleteByUserId("user123");
    }
}
