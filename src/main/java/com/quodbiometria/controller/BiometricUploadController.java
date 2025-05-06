package com.quodbiometria.controller;

import com.quodbiometria.model.dto.request.BiometricImageUploadRequestDTO;
import com.quodbiometria.model.dto.response.ApiResponseDTO;
import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.service.BiometricImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/biometria")
@RequiredArgsConstructor
public class BiometricUploadController {

    private final BiometricImageStorageService storageService;

    @PostMapping("/facial/upload")
    public ResponseEntity<ApiResponseDTO<BiometricImageMetadataResponseDTO>> uploadFacialImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(value = "dispositivo", required = false) String dispositivo) {

        BiometricImageUploadRequestDTO requestDTO = BiometricImageUploadRequestDTO.builder()
                .usuarioId(usuarioId)
                .tipoImagem("FACIAL")
                .dispositivo(dispositivo)
                .build();

        Map<String, String> metadata = new HashMap<>();
        if (dispositivo != null) {
            metadata.put("dispositivo", dispositivo);
        }

        BiometricImageMetadataResponseDTO responseDTO = storageService.storeImage(
                file, requestDTO, metadata
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Imagem facial armazenada com sucesso", responseDTO));
    }

    @PostMapping("/digital/upload")
    public ResponseEntity<ApiResponseDTO<BiometricImageMetadataResponseDTO>> uploadFingerprintImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(value = "dedo", required = false) String dedo,
            @RequestParam(value = "dispositivo", required = false) String dispositivo) {

        BiometricImageUploadRequestDTO requestDTO = BiometricImageUploadRequestDTO.builder()
                .usuarioId(usuarioId)
                .tipoImagem("DIGITAL")
                .dispositivo(dispositivo)
                .build();

        Map<String, String> metadata = new HashMap<>();
        if (dispositivo != null) {
            metadata.put("dispositivo", dispositivo);
        }
        if (dedo != null) {
            metadata.put("dedo", dedo);
        }

        BiometricImageMetadataResponseDTO responseDTO = storageService.storeImage(
                file, requestDTO, metadata
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Impressão digital armazenada com sucesso", responseDTO));
    }

    @PostMapping("/documento/upload")
    public ResponseEntity<ApiResponseDTO<BiometricImageMetadataResponseDTO>> uploadDocumentImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam(value = "dispositivo", required = false) String dispositivo) {

        BiometricImageUploadRequestDTO requestDTO = BiometricImageUploadRequestDTO.builder()
                .usuarioId(usuarioId)
                .tipoImagem("DOCUMENTO")
                .dispositivo(dispositivo)
                .build();

        Map<String, String> metadata = new HashMap<>();
        if (dispositivo != null) {
            metadata.put("dispositivo", dispositivo);
        }
        metadata.put("tipoDocumento", tipoDocumento);

        BiometricImageMetadataResponseDTO responseDTO = storageService.storeImage(
                file, requestDTO, metadata
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Imagem de documento armazenada com sucesso", responseDTO));
    }
}