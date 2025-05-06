package com.quodbiometria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.quodbiometria.exception.ImageValidationException;
import com.quodbiometria.model.dto.request.BiometricImageUploadRequestDTO;
import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.model.entity.BiometricImageMetadata;
import com.quodbiometria.model.mappers.BiometricImageMetadataMapper;
import com.quodbiometria.repository.BiometricImageMetadataRepository;

@ExtendWith(MockitoExtension.class)
class BiometricImageStorageServiceTest {

    @InjectMocks
    private BiometricImageStorageService biometricImageStorageService;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private BiometricImageMetadataRepository metadataRepository;

    @Mock
    private BiometricImageMetadataMapper mapper;

    @Mock
    private ImageValidationService imageValidationService;

    @Mock
    private ImageMetadataExtractionService metadataExtractionService;

    @Mock
    private GridFSFile gridFSFile;

    @Mock
    private GridFsResource gridFsResource;

    private MockMultipartFile testImageFile;
    private BiometricImageUploadRequestDTO uploadRequestDTO;
    private BiometricImageMetadata testMetadata;
    private BiometricImageMetadataResponseDTO testMetadataResponse;
    private Map<String, String> exifMetadata;
    private Map<String, String> sanitizedExifMetadata;

    // ID MongoDB válido (24 caracteres hexadecimais)
    private static final String VALID_FILE_ID = "507f1f77bcf86cd799439011";
    private static final String VALID_META_ID = "507f1f77bcf86cd799439022";

    @BeforeEach
    void setUp() {
        testImageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        uploadRequestDTO = new BiometricImageUploadRequestDTO();
        uploadRequestDTO.setUsuarioId("user123");
        uploadRequestDTO.setTipoImagem("FACIAL");
        uploadRequestDTO.setDispositivo("MOBILE");

        exifMetadata = new HashMap<>();
        exifMetadata.put("camera.modelo", "Test Camera");
        exifMetadata.put("resolucao.x", "300");

        sanitizedExifMetadata = new HashMap<>();
        sanitizedExifMetadata.put("camera.modelo", "Test Camera");
        sanitizedExifMetadata.put("resolucao.x", "300");

        testMetadata = new BiometricImageMetadata();
        testMetadata.setId(VALID_META_ID);
        testMetadata.setFileId(VALID_FILE_ID);
        testMetadata.setFilename("test-image.jpg");
        testMetadata.setContentType("image/jpeg");
        testMetadata.setSize(1024);
        testMetadata.setUsuarioId("user123");
        testMetadata.setTipoImagem("FACIAL");
        testMetadata.setDispositivo("MOBILE");
        testMetadata.setDataCriacao(LocalDateTime.now());
        testMetadata.setDataAtualizacao(LocalDateTime.now());
        testMetadata.setHash("testhash123");
        testMetadata.setAtiva(true);
        testMetadata.setExifMetadata(sanitizedExifMetadata);

        testMetadataResponse = new BiometricImageMetadataResponseDTO();
        testMetadataResponse.setId(VALID_META_ID);
        testMetadataResponse.setFilename("test-image.jpg");
        testMetadataResponse.setContentType("image/jpeg");
        testMetadataResponse.setSize(1024);
        testMetadataResponse.setUsuarioId("user123");
        testMetadataResponse.setTipoImagem("FACIAL");
        testMetadataResponse.setDispositivo("MOBILE");
        testMetadataResponse.setDataCriacao(LocalDateTime.now());
        testMetadataResponse.setDataAtualizacao(LocalDateTime.now());
        testMetadataResponse.setAtiva(true);
        testMetadataResponse.setExifMetadata(sanitizedExifMetadata);
    }

    @Test
    void testStoreImage_ValidImage_Success(){
        ObjectId validObjectId = new ObjectId(VALID_FILE_ID);

        doNothing().when(imageValidationService).validateImage(any(), anyString());
        when(metadataExtractionService.extractMetadata(any())).thenReturn(exifMetadata);
        when(metadataExtractionService.sanitizeMetadata(any())).thenReturn(sanitizedExifMetadata);
        when(metadataRepository.findByHashAndUsuarioId(anyString(), anyString())).thenReturn(List.of());
        when(gridFsTemplate.store(any(), anyString(), anyString(), any(Document.class)))
                .thenReturn(validObjectId);
        when(metadataRepository.save(any(BiometricImageMetadata.class))).thenReturn(testMetadata);
        when(mapper.toDTO(testMetadata)).thenReturn(testMetadataResponse);

        BiometricImageMetadataResponseDTO result = biometricImageStorageService.storeImage(
                testImageFile, uploadRequestDTO, new HashMap<>());

        assertNotNull(result);
        assertEquals(VALID_META_ID, result.getId());
        assertEquals("test-image.jpg", result.getFilename());
        assertEquals("user123", result.getUsuarioId());

        verify(imageValidationService).validateImage(testImageFile, "FACIAL");
        verify(metadataExtractionService).extractMetadata(testImageFile);
        verify(metadataExtractionService).sanitizeMetadata(exifMetadata);
        verify(gridFsTemplate).store(any(), eq("test-image.jpg"), eq("image/jpeg"), any(Document.class));
        verify(metadataRepository).save(any(BiometricImageMetadata.class));
    }

    @Test
    void testStoreImage_ValidationFails_ThrowsException(){
        doThrow(new ImageValidationException("Validation failed"))
                .when(imageValidationService).validateImage(any(), anyString());


        Map<String, String> metadataMap = new HashMap<>();

        assertThrows(ImageValidationException.class, () ->
                biometricImageStorageService.storeImage(testImageFile, uploadRequestDTO, metadataMap));

        verify(imageValidationService).validateImage(testImageFile, "FACIAL");
        verifyNoInteractions(gridFsTemplate);
        verifyNoInteractions(metadataRepository);
    }


    @Test
    void testGetImage_ExistingId_ReturnsImageBytes() throws Exception {
        byte[] expectedBytes = "test image content".getBytes();
        when(metadataRepository.findById(VALID_META_ID)).thenReturn(Optional.of(testMetadata));
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(gridFSFile);
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn(gridFsResource);
        when(gridFsResource.getInputStream()).thenReturn(new ByteArrayInputStream(expectedBytes));

        byte[] result = biometricImageStorageService.getImage(VALID_META_ID);

        assertArrayEquals(expectedBytes, result);
        verify(metadataRepository).findById(VALID_META_ID);
        verify(gridFsTemplate).findOne(any(Query.class));
        verify(gridFsTemplate).getResource(gridFSFile);
    }

    @Test
    void testGetImage_NonExistingId_ThrowsException() {
        String nonExistentId = "507f1f77bcf86cd799439033";
        when(metadataRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> biometricImageStorageService.getImage(nonExistentId));
        verify(metadataRepository).findById(nonExistentId);
    }

    @Test
    void testGetMetadataById_ExistingId_ReturnsMetadata() {
        when(metadataRepository.findById(VALID_META_ID)).thenReturn(Optional.of(testMetadata));
        when(mapper.toDTO(testMetadata)).thenReturn(testMetadataResponse);

        BiometricImageMetadataResponseDTO result = biometricImageStorageService.getMetadataById(VALID_META_ID);

        assertNotNull(result);
        assertEquals(VALID_META_ID, result.getId());
        assertEquals("test-image.jpg", result.getFilename());
        verify(metadataRepository).findById(VALID_META_ID);
        verify(mapper).toDTO(testMetadata);
    }

    @Test
    void testGetMetadataById_NonExistingId_ThrowsException() {
        String nonExistentId = "507f1f77bcf86cd799439033";
        when(metadataRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> biometricImageStorageService.getMetadataById(nonExistentId));
        verify(metadataRepository).findById(nonExistentId);
    }

    @Test
    void testGetImagesByUsuario_ReturnsImages() {
        List<BiometricImageMetadata> metadataList = Collections.singletonList(testMetadata);
        List<BiometricImageMetadataResponseDTO> expectedResponses = Collections.singletonList(testMetadataResponse);

        when(metadataRepository.findByUsuarioId("user123")).thenReturn(metadataList);
        when(mapper.toDTOList(metadataList)).thenReturn(expectedResponses);

        List<BiometricImageMetadataResponseDTO> result = biometricImageStorageService.getImagesByUsuario("user123");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_META_ID, result.get(0).getId());
        verify(metadataRepository).findByUsuarioId("user123");
        verify(mapper).toDTOList(metadataList);
    }

    @Test
    void testGetImagesByUsuarioAndTipo_ReturnsImages() {
        List<BiometricImageMetadata> metadataList = Collections.singletonList(testMetadata);
        List<BiometricImageMetadataResponseDTO> expectedResponses = Collections.singletonList(testMetadataResponse);

        when(metadataRepository.findByUsuarioIdAndTipoImagem("user123", "FACIAL")).thenReturn(metadataList);
        when(mapper.toDTOList(metadataList)).thenReturn(expectedResponses);

        List<BiometricImageMetadataResponseDTO> result = biometricImageStorageService.getImagesByUsuarioAndTipo("user123", "FACIAL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_META_ID, result.get(0).getId());
        verify(metadataRepository).findByUsuarioIdAndTipoImagem("user123", "FACIAL");
        verify(mapper).toDTOList(metadataList);
    }

    @Test
    void testDeleteImage_ExistingId_DeletesImage() {
        when(metadataRepository.findById(VALID_META_ID)).thenReturn(Optional.of(testMetadata));

        biometricImageStorageService.deleteImage(VALID_META_ID);

        verify(metadataRepository).findById(VALID_META_ID);
        verify(gridFsTemplate).delete(any(Query.class));
        verify(metadataRepository).delete(testMetadata);
    }

    @Test
    void testDeleteImage_NonExistingId_ThrowsException() {
        String nonExistentId = "507f1f77bcf86cd799439033";
        when(metadataRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> biometricImageStorageService.deleteImage(nonExistentId));
        verify(metadataRepository).findById(nonExistentId);
        verifyNoMoreInteractions(gridFsTemplate);
    }
}