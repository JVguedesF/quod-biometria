package com.quodbiometria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageMetadataExtractionServiceTest {

    @InjectMocks
    private ImageMetadataExtractionService metadataExtractionService;

    private MockMultipartFile testImageFile;
    private JpegImageMetadata jpegMetadata;
    private TiffImageMetadata exifMetadata;

    @BeforeEach
    void setUp() {
        testImageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        jpegMetadata = mock(JpegImageMetadata.class);
        exifMetadata = mock(TiffImageMetadata.class);
    }

    @Test
    void testExtractMetadata_ValidImage_ReturnsMetadata() {
        try (MockedStatic<Imaging> imagingMock = mockStatic(Imaging.class)) {
            imagingMock.when(() -> Imaging.getMetadata(any(ByteArrayInputStream.class), anyString()))
                    .thenReturn(jpegMetadata);

            when(jpegMetadata.getExif()).thenReturn(exifMetadata);

            Map<String, String> result = metadataExtractionService.extractMetadata(testImageFile);

            assertNotNull(result);
            assertTrue(result.containsKey("filename"));
            assertTrue(result.containsKey("contentType"));
            assertTrue(result.containsKey("size"));
            assertTrue(result.containsKey("extractedAt"));

            assertEquals("test-image.jpg", result.get("filename"));
            assertEquals("image/jpeg", result.get("contentType"));
        }
    }

    @Test
    void testExtractMetadata_ExceptionThrown_ReturnsBasicMetadata() {
        try (MockedStatic<Imaging> imagingMock = mockStatic(Imaging.class)) {
            imagingMock.when(() -> Imaging.getMetadata(any(ByteArrayInputStream.class), anyString()))
                    .thenThrow(new ImageReadException("Test exception"));

            Map<String, String> result = metadataExtractionService.extractMetadata(testImageFile);

            assertNotNull(result);
            assertTrue(result.containsKey("filename"));
            assertTrue(result.containsKey("contentType"));
            assertTrue(result.containsKey("size"));
            assertTrue(result.containsKey("extractedAt"));
            assertTrue(result.containsKey("error"));

            assertEquals("test-image.jpg", result.get("filename"));
            assertEquals("image/jpeg", result.get("contentType"));
            assertTrue(result.get("error").contains("Não foi possível extrair metadados EXIF"));
        }
    }

    @Test
    void testSanitizeMetadata_PersonalImageType_RemovesGpsData() {
        Map<String, String> metadata = Map.of(
                "filename", "test-image.jpg",
                "tipoImagem", "FACIAL",
                "gps.latitude", "12.345",
                "gps.longitude", "67.890",
                "camera.modelo", "Test Camera"
        );

        Map<String, String> result = metadataExtractionService.sanitizeMetadata(metadata);

        assertNotNull(result);
        assertFalse(result.containsKey("gps.latitude"));
        assertFalse(result.containsKey("gps.longitude"));
        assertTrue(result.containsKey("camera.modelo"));
        assertEquals("Test Camera", result.get("camera.modelo"));
    }

    @Test
    void testSanitizeMetadata_NonPersonalImageType_KeepsGpsData() {
        Map<String, String> metadata = Map.of(
                "filename", "test-image.jpg",
                "tipoImagem", "DOCUMENTO",
                "gps.latitude", "12.345",
                "gps.longitude", "67.890",
                "camera.modelo", "Test Camera"
        );

        Map<String, String> result = metadataExtractionService.sanitizeMetadata(metadata);

        assertNotNull(result);
        assertTrue(result.containsKey("gps.latitude"));
        assertTrue(result.containsKey("gps.longitude"));
        assertTrue(result.containsKey("camera.modelo"));
        assertEquals("12.345", result.get("gps.latitude"));
        assertEquals("67.890", result.get("gps.longitude"));
        assertEquals("Test Camera", result.get("camera.modelo"));
    }

    @Test
    void testSanitizeMetadata_LongValues_Truncates() {
        String longValue = "a".repeat(300);
        Map<String, String> metadata = Map.of(
                "filename", "test-image.jpg",
                "longField", longValue
        );

        Map<String, String> result = metadataExtractionService.sanitizeMetadata(metadata);

        assertNotNull(result);
        assertTrue(result.containsKey("longField"));
        assertEquals(255, result.get("longField").length());
        assertTrue(result.get("longField").endsWith("..."));
    }
}