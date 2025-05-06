package com.quodbiometria.util;

import com.quodbiometria.model.entity.RefreshToken;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration:3600000}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:86400000}")
    private Long refreshTokenExpiration;

    private final Map<String, SecretKey> keyStore = new ConcurrentHashMap<>();
    private String currentKeyId;

    @PostConstruct
    public void init() {
        rotateKey();
    }

    private synchronized void rotateKey() {
        String keyId = UUID.randomUUID().toString();
        SecretKey key = Keys.hmacShaKeyFor(generateRandomKey());
        keyStore.put(keyId, key);
        currentKeyId = keyId;
        log.info("JWT signing key rotated, new key ID: {}", keyId);
    }

    private byte[] generateRandomKey() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        return key;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledKeyRotation() {
        rotateKey();
        log.info("Scheduled JWT key rotation completed");
    }

    private Key getSigningKey(String keyId) {
        if (keyId == null || !keyStore.containsKey(keyId)) {
            return keyStore.get(currentKeyId);
        }
        return keyStore.get(keyId);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKeyResolver(keyResolver())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private io.jsonwebtoken.SigningKeyResolver keyResolver() {
        return new io.jsonwebtoken.SigningKeyResolverAdapter() {
            @Override
            public Key resolveSigningKey(io.jsonwebtoken.JwsHeader header, Claims claims) {
                String keyId = header.getKeyId();
                return getSigningKey(keyId);
            }
        };
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(((User) userDetails).getId())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .keyId(currentKeyId)
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setHeaderParam("kid", currentKeyId)
                .signWith(getSigningKey(currentKeyId), SignatureAlgorithm.HS512)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}