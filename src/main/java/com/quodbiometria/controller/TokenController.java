package com.quodbiometria.controller;

import com.quodbiometria.model.dto.request.RefreshTokenRequestDTO;
import com.quodbiometria.model.dto.response.ApiResponseDTO;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {

        AuthenticationResponseDTO tokenResponse = refreshTokenService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Token atualizado com sucesso",
                tokenResponse
        ));
    }
}