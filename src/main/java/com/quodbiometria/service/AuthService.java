package com.quodbiometria.service;

import com.quodbiometria.exception.RateLimitExceededException;
import com.quodbiometria.model.dto.request.AuthenticationRequestDTO;
import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.model.mappers.UserMapper;
import com.quodbiometria.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RateLimitService rateLimitService;

    @Value("${jwt.access-token.expiration:3600000}")
    private Long accessTokenExpiration;

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        String clientIp = getClientIp();
        String rateLimitKey = "login_" + clientIp + "_" + request.getEmail();

        if (!rateLimitService.tryConsume(rateLimitKey)) {
            long waitTimeSeconds = 60;
            log.warn("Rate limit exceeded for login attempts: {}", rateLimitKey);
            throw new RateLimitExceededException("Muitas tentativas de login. Por favor, tente novamente após "
                    + waitTimeSeconds + " segundos.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return AuthenticationResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000)
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw e;
        }
    }

    public AuthenticationResponseDTO register(UserCreateDTO request) {
        User user = userMapper.toEntity(request);
        userService.saveUser(user);

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }

    private String getClientIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}