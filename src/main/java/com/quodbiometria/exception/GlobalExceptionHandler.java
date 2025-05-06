package com.quodbiometria.exception;

import com.quodbiometria.model.dto.response.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ImageValidationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleImageValidationException(ImageValidationException ex) {
        log.error("Erro de validação de imagem: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDTO<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleTokenRefreshException(TokenRefreshException ex) {
        log.error("Erro de refresh token: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDTO<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.error("Limite de requisições excedido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiResponseDTO<>(false, ex.getMessage(), null));
    }

}