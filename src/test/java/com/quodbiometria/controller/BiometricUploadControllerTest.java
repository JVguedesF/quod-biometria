package com.quodbiometria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.quodbiometria.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.quodbiometria.model.dto.request.BiometricImageUploadRequestDTO;
import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.service.BiometricImageStorageService;

@WebMvcTest(BiometricUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class BiometricUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private BiometricImageStorageService storageService;

    private MockMultipartFile testImageFile;
    private BiometricImageMetadataResponseDTO metadataResponse;
    private Map<String, String> exifMetadata;

    @BeforeEach
    void setUp() {
        testImageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        exifMetadata = new HashMap<>();
        exifMetadata.put("camera.modelo", "Test Camera");
        exifMetadata.put("resolucao.x", "300");

        metadataResponse = new BiometricImageMetadataResponseDTO();
        metadataResponse.setId("meta123");
        metadataResponse.setFilename("test-image.jpg");
        metadataResponse.setContentType("image/jpeg");
        metadataResponse.setSize(1024);
        metadataResponse.setUsuarioId("user123");
        metadataResponse.setTipoImagem("FACIAL");
        metadataResponse.setDispositivo("MOBILE");
        metadataResponse.setDataCriacao(LocalDateTime.now());
        metadataResponse.setDataAtualizacao(LocalDateTime.now());
        metadataResponse.setAtiva(true);
        metadataResponse.setExifMetadata(exifMetadata);
    }

    @Test
    @WithMockUser
    void testUploadFacialImage_ValidImage_ReturnsMetadata() throws Exception {
        when(storageService.storeImage(
                any(),
                any(BiometricImageUploadRequestDTO.class),
                anyMap()))
                .thenReturn(metadataResponse);

        mockMvc.perform(multipart("/api/biometria/facial/upload")
                        .file(testImageFile)
                        .param("usuarioId", "user123")
                        .param("dispositivo", "MOBILE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagem facial armazenada com sucesso"))
                .andExpect(jsonPath("$.data.id").value("meta123"))
                .andExpect(jsonPath("$.data.filename").value("test-image.jpg"))
                .andExpect(jsonPath("$.data.usuarioId").value("user123"))
                .andExpect(jsonPath("$.data.usuarioId").value("user123"))
                .andExpect(jsonPath("$.data.tipoImagem").value("FACIAL"))
                .andExpect(jsonPath("$.data.dispositivo").value("MOBILE"));
    }

    @Test
    @WithMockUser
    void testUploadFingerprintImage_ValidImage_ReturnsMetadata() throws Exception {
        BiometricImageMetadataResponseDTO fingerprintResponse = new BiometricImageMetadataResponseDTO();
        fingerprintResponse.setId("meta456");
        fingerprintResponse.setFilename("test-fingerprint.jpg");
        fingerprintResponse.setContentType("image/jpeg");
        fingerprintResponse.setSize(1024);
        fingerprintResponse.setUsuarioId("user123");
        fingerprintResponse.setTipoImagem("DIGITAL");
        fingerprintResponse.setDispositivo("SCANNER");
        fingerprintResponse.setDataCriacao(LocalDateTime.now());
        fingerprintResponse.setDataAtualizacao(LocalDateTime.now());
        fingerprintResponse.setAtiva(true);
        fingerprintResponse.setExifMetadata(exifMetadata);

        when(storageService.storeImage(
                any(),
                any(BiometricImageUploadRequestDTO.class),
                anyMap()))
                .thenReturn(fingerprintResponse);

        mockMvc.perform(multipart("/api/biometria/digital/upload")
                        .file(testImageFile)
                        .param("usuarioId", "user123")
                        .param("dedo", "POLEGAR_DIREITO")
                        .param("dispositivo", "SCANNER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Impressão digital armazenada com sucesso"))
                .andExpect(jsonPath("$.data.id").value("meta456"))
                .andExpect(jsonPath("$.data.filename").value("test-fingerprint.jpg"))
                .andExpect(jsonPath("$.data.usuarioId").value("user123"))
                .andExpect(jsonPath("$.data.tipoImagem").value("DIGITAL"))
                .andExpect(jsonPath("$.data.dispositivo").value("SCANNER"));
    }

    @Test
    @WithMockUser
    void testUploadDocumentImage_ValidImage_ReturnsMetadata() throws Exception {
        BiometricImageMetadataResponseDTO documentResponse = new BiometricImageMetadataResponseDTO();
        documentResponse.setId("meta789");
        documentResponse.setFilename("test-document.jpg");
        documentResponse.setContentType("image/jpeg");
        documentResponse.setSize(1024);
        documentResponse.setUsuarioId("user123");
        documentResponse.setTipoImagem("DOCUMENTO");
        documentResponse.setDispositivo("MOBILE");
        documentResponse.setDataCriacao(LocalDateTime.now());
        documentResponse.setDataAtualizacao(LocalDateTime.now());
        documentResponse.setAtiva(true);
        documentResponse.setExifMetadata(exifMetadata);

        when(storageService.storeImage(
                any(),
                any(BiometricImageUploadRequestDTO.class),
                anyMap()))
                .thenReturn(documentResponse);

        mockMvc.perform(multipart("/api/biometria/documento/upload")
                        .file(testImageFile)
                        .param("usuarioId", "user123")
                        .param("tipoDocumento", "RG")
                        .param("dispositivo", "MOBILE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagem de documento armazenada com sucesso"))
                .andExpect(jsonPath("$.data.id").value("meta789"))
                .andExpect(jsonPath("$.data.filename").value("test-document.jpg"))
                .andExpect(jsonPath("$.data.usuarioId").value("user123"))
                .andExpect(jsonPath("$.data.tipoImagem").value("DOCUMENTO"))
                .andExpect(jsonPath("$.data.dispositivo").value("MOBILE"));
    }
}
