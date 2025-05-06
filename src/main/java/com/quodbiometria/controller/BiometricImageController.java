package com.quodbiometria.controller;

import com.quodbiometria.model.dto.request.BiometricImageUploadRequestDTO;
import com.quodbiometria.model.dto.response.ApiResponseDTO;
import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.service.BiometricImageStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/biometria/imagens")
@RequiredArgsConstructor
public class BiometricImageController {

    private final BiometricImageStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponseDTO<BiometricImageMetadataResponseDTO>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute BiometricImageUploadRequestDTO requestDTO) {

        Map<String, String> metadata = new HashMap<>();
        if (requestDTO.getDispositivo() != null) {
            metadata.put("dispositivo", requestDTO.getDispositivo());
        }

        BiometricImageMetadataResponseDTO responseDTO = storageService.storeImage(
                file, requestDTO, metadata
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Imagem biométrica armazenada com sucesso", responseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        byte[] imageBytes = storageService.getImage(id);
        BiometricImageMetadataResponseDTO metadata = storageService.getMetadataById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponseDTO<List<BiometricImageMetadataResponseDTO>>> getImagesByUsuario(
            @PathVariable String usuarioId) {

        List<BiometricImageMetadataResponseDTO> images = storageService.getImagesByUsuario(usuarioId);

        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Imagens biométricas recuperadas com sucesso", images));
    }

    @GetMapping("/usuario/{usuarioId}/tipo/{tipoImagem}")
    public ResponseEntity<ApiResponseDTO<List<BiometricImageMetadataResponseDTO>>> getImagesByUsuarioAndTipo(
            @PathVariable String usuarioId,
            @PathVariable String tipoImagem) {

        List<BiometricImageMetadataResponseDTO> images =
                storageService.getImagesByUsuarioAndTipo(usuarioId, tipoImagem);

        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Imagens biométricas recuperadas com sucesso", images));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteImage(@PathVariable String id) {
        storageService.deleteImage(id);

        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Imagem biométrica excluída com sucesso", null));
    }
}