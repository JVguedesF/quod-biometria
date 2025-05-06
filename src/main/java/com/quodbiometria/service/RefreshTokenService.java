package com.quodbiometria.service;

import com.quodbiometria.exception.TokenRefreshException;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.model.entity.RefreshToken;
import com.quodbiometria.repository.RefreshTokenRepository;
import com.quodbiometria.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.access-token.expiration:3600000}")
    private Long accessTokenExpiration;

    @Transactional
    public AuthenticationResponseDTO refreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

        RefreshToken token = tokenOpt.orElseThrow(() ->
                new TokenRefreshException("Refresh token não encontrado"));

        if (token.isExpired() || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expirado ou revogado");
        }

        UserDetails userDetails = userService.loadUserById(token.getUserId());

        String newAccessToken = jwtUtil.generateToken(userDetails);

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthenticationResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }

    @SuppressWarnings("unused")
    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId)
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @SuppressWarnings("unused")
    @Transactional
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}