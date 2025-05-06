package com.quodbiometria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.*;

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

@WebMvcTest(BiometricImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class BiometricImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private BiometricImageStorageService storageService;

    private MockMultipartFile testImageFile;
    private BiometricImageMetadataResponseDTO metadataResponse;

    @BeforeEach
    void setUp() {
        testImageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        Map<String, String> exifMetadata = new HashMap<>();
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
    void testUploadImage_ValidImage_ReturnsMetadata() throws Exception {
        when(storageService.storeImage(any(), any(BiometricImageUploadRequestDTO.class), anyMap()))
                .thenReturn(metadataResponse);

        mockMvc.perform(multipart("/api/biometria/imagens/upload")
                        .file(testImageFile)
                        .param("usuarioId", "user123")
                        .param("tipoImagem", "FACIAL")
                        .param("dispositivo", "MOBILE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagem biométrica armazenada com sucesso"))
                .andExpect(jsonPath("$.data.id").value("meta123"))
                .andExpect(jsonPath("$.data.filename").value("test-image.jpg"))
                .andExpect(jsonPath("$.data.usuarioId").value("user123"))
                .andExpect(jsonPath("$.data.tipoImagem").value("FACIAL"))
                .andExpect(jsonPath("$.data.dispositivo").value("MOBILE"));
    }

    @Test
    @WithMockUser
    void testGetImage_ExistingId_ReturnsImage() throws Exception {
        byte[] imageBytes = "test image content".getBytes();
        when(storageService.getImage("meta123")).thenReturn(imageBytes);
        when(storageService.getMetadataById("meta123")).thenReturn(metadataResponse);

        mockMvc.perform(get("/api/biometria/imagens/meta123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    @WithMockUser
    void testGetImagesByUsuario_ReturnsImages() throws Exception {
        List<BiometricImageMetadataResponseDTO> images = Collections.singletonList(metadataResponse);
        when(storageService.getImagesByUsuario("user123")).thenReturn(images);

        mockMvc.perform(get("/api/biometria/imagens/usuario/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagens biométricas recuperadas com sucesso"))
                .andExpect(jsonPath("$.data[0].id").value("meta123"))
                .andExpect(jsonPath("$.data[0].filename").value("test-image.jpg"))
                .andExpect(jsonPath("$.data[0].usuarioId").value("user123"));
    }

    @Test
    @WithMockUser
    void testGetImagesByUsuarioAndTipo_ReturnsImages() throws Exception {
        List<BiometricImageMetadataResponseDTO> images = Collections.singletonList(metadataResponse);
        when(storageService.getImagesByUsuarioAndTipo("user123", "FACIAL")).thenReturn(images);

        mockMvc.perform(get("/api/biometria/imagens/usuario/user123/tipo/FACIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagens biométricas recuperadas com sucesso"))
                .andExpect(jsonPath("$.data[0].id").value("meta123"))
                .andExpect(jsonPath("$.data[0].filename").value("test-image.jpg"))
                .andExpect(jsonPath("$.data[0].usuarioId").value("user123"))
                .andExpect(jsonPath("$.data[0].tipoImagem").value("FACIAL"));
    }

    @Test
    @WithMockUser
    void testDeleteImage_ExistingId_ReturnsSuccess() throws Exception {
        doNothing().when(storageService).deleteImage("meta123");

        mockMvc.perform(delete("/api/biometria/imagens/meta123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagem biométrica excluída com sucesso"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
