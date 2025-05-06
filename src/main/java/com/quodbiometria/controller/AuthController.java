package com.quodbiometria.controller;

import com.quodbiometria.model.dto.request.AuthenticationRequestDTO;
import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.response.ApiResponseDTO;
import com.quodbiometria.model.dto.response.AuthenticationResponseDTO;
import com.quodbiometria.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponseDTO>> authenticate(
            @RequestBody AuthenticationRequestDTO request
    ) {
        AuthenticationResponseDTO response = authService.authenticate(request);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Authentication successful",
                response
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponseDTO>> register(
            @RequestBody UserCreateDTO request
    ) {
        AuthenticationResponseDTO response = authService.register(request);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Registration successful",
                response
        ));
    }
}