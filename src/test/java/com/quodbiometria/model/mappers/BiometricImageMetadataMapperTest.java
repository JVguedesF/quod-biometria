package com.quodbiometria.model.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.model.entity.BiometricImageMetadata;

class BiometricImageMetadataMapperTest {

    private BiometricImageMetadataMapper mapper;

    private BiometricImageMetadata testMetadata;
    private Map<String, String> exifMetadata;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mapper = new BiometricImageMetadataMapper();
        now = LocalDateTime.now();

        exifMetadata = new HashMap<>();
        exifMetadata.put("camera.modelo", "Test Camera");
        exifMetadata.put("resolucao.x", "300");

        testMetadata = new BiometricImageMetadata();
        testMetadata.setId("meta123");
        testMetadata.setFileId("file123");
        testMetadata.setFilename("test-image.jpg");
        testMetadata.setContentType("image/jpeg");
        testMetadata.setSize(1024);
        testMetadata.setUsuarioId("user123");
        testMetadata.setTipoImagem("FACIAL");
        testMetadata.setDispositivo("MOBILE");
        testMetadata.setDataCriacao(now);
        testMetadata.setDataAtualizacao(now);
        testMetadata.setHash("testhash123");
        testMetadata.setAtiva(true);
        testMetadata.setExifMetadata(exifMetadata);
    }

    @Test
    void testToDTO_ValidEntity_ReturnsDTO() {
        // Act
        BiometricImageMetadataResponseDTO result = mapper.toDTO(testMetadata);

        // Assert
        assertNotNull(result);
        assertEquals("meta123", result.getId());
        assertEquals("test-image.jpg", result.getFilename());
        assertEquals("image/jpeg", result.getContentType());
        assertEquals(1024, result.getSize());
        assertEquals("user123", result.getUsuarioId());
        assertEquals("FACIAL", result.getTipoImagem());
        assertEquals("MOBILE", result.getDispositivo());
        assertEquals(now, result.getDataCriacao());
        assertEquals(now, result.getDataAtualizacao());
        assertTrue(result.isAtiva());
        assertNotNull(result.getExifMetadata());
        assertEquals("Test Camera", result.getExifMetadata().get("camera.modelo"));
        assertEquals("300", result.getExifMetadata().get("resolucao.x"));
    }

    @Test
    void testToDTO_NullEntity_ReturnsNull() {
        // Act
        BiometricImageMetadataResponseDTO result = mapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToDTOList_ValidEntities_ReturnsDTOs() {
        // Arrange
        List<BiometricImageMetadata> metadataList = Collections.singletonList(testMetadata);

        // Act
        List<BiometricImageMetadataResponseDTO> result = mapper.toDTOList(metadataList);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("meta123", result.get(0).getId());
        assertEquals("test-image.jpg", result.get(0).getFilename());
    }

    @Test
    void testToDTOList_NullList_ReturnsEmptyList() {
        // Act
        List<BiometricImageMetadataResponseDTO> result = mapper.toDTOList(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToEntity_ValidDTO_ReturnsEntity() {
        // Arrange
        BiometricImageMetadataResponseDTO dto = new BiometricImageMetadataResponseDTO();
        dto.setId("meta123");
        dto.setFilename("test-image.jpg");
        dto.setContentType("image/jpeg");
        dto.setSize(1024);
        dto.setUsuarioId("user123");
        dto.setTipoImagem("FACIAL");
        dto.setDispositivo("MOBILE");
        dto.setDataCriacao(now);
        dto.setDataAtualizacao(now);
        dto.setAtiva(true);
        dto.setExifMetadata(exifMetadata);

        // Act
        BiometricImageMetadata result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals("meta123", result.getId());
        assertEquals("test-image.jpg", result.getFilename());
        assertEquals("image/jpeg", result.getContentType());
        assertEquals(1024, result.getSize());
        assertEquals("user123", result.getUsuarioId());
        assertEquals("FACIAL", result.getTipoImagem());
        assertEquals("MOBILE", result.getDispositivo());
        assertEquals(now, result.getDataCriacao());
        assertEquals(now, result.getDataAtualizacao());
        assertTrue(result.isAtiva());
        assertNotNull(result.getExifMetadata());
        assertEquals("Test Camera", result.getExifMetadata().get("camera.modelo"));
        assertEquals("300", result.getExifMetadata().get("resolucao.x"));
    }

    @Test
    void testToEntity_NullDTO_ReturnsNull() {
        // Act
        BiometricImageMetadata result = mapper.toEntity(null);

        // Assert
        assertNull(result);
    }
}
