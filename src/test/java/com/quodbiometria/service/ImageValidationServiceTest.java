package com.quodbiometria.service;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.quodbiometria.exception.ImageValidationException;

@ExtendWith(MockitoExtension.class)
class ImageValidationServiceTest {

    @InjectMocks
    private ImageValidationService imageValidationService;

    private MockMultipartFile validJpegFile;
    private MockMultipartFile validPngFile;
    private MockMultipartFile invalidFormatFile;
    private MockMultipartFile emptyFile;
    private MockMultipartFile oversizedFile;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(imageValidationService, "maxFileSize", 5242880L);
        ReflectionTestUtils.setField(imageValidationService, "minWidth", 100);
        ReflectionTestUtils.setField(imageValidationService, "minHeight", 100);
        ReflectionTestUtils.setField(imageValidationService, "allowedFormats", new String[]{"jpeg", "jpg", "png"});

        BufferedImage jpegImage = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        byte[] jpegBytes = createImageBytes(jpegImage, "jpg");
        validJpegFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", jpegBytes);

        BufferedImage pngImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        byte[] pngBytes = createImageBytes(pngImage, "png");
        validPngFile = new MockMultipartFile("image", "test.png", "image/png", pngBytes);

        invalidFormatFile = new MockMultipartFile("image", "test.txt", "text/plain", "This is not an image".getBytes());

        emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        oversizedFile = new MockMultipartFile("image", "large.jpg", "image/jpeg", new byte[6 * 1024 * 1024]);
    }

    private byte[] createImageBytes(BufferedImage image, String format) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    void testValidateImage_ValidJpeg_Success() {
        assertDoesNotThrow(() -> imageValidationService.validateImage(validJpegFile, "FACIAL"));
    }

    @Test
    void testValidateImage_ValidPng_Success() {
        assertDoesNotThrow(() -> imageValidationService.validateImage(validPngFile, "DIGITAL"));
    }

    @Test
    void testValidateImage_EmptyFile_ThrowsException() {
        ImageValidationException exception = assertThrows(ImageValidationException.class,
                () -> imageValidationService.validateImage(emptyFile, "FACIAL"));

        assertEquals("O arquivo da imagem é obrigatório", exception.getMessage());
    }

    @Test
    void testValidateImage_InvalidFormat_ThrowsException() {
        ImageValidationException exception = assertThrows(ImageValidationException.class,
                () -> imageValidationService.validateImage(invalidFormatFile, "FACIAL"));

        assertTrue(exception.getMessage().contains("Formato de arquivo não permitido"));
    }

    @Test
    void testValidateImage_OversizedFile_ThrowsException() {
        ImageValidationException exception = assertThrows(ImageValidationException.class,
                () -> imageValidationService.validateImage(oversizedFile, "FACIAL"));

        assertTrue(exception.getMessage().contains("O tamanho do arquivo excede o limite máximo permitido"));
    }

    @Test
    void testValidateImage_NullFile_ThrowsException() {
        ImageValidationException exception = assertThrows(ImageValidationException.class,
                () -> imageValidationService.validateImage(null, "FACIAL"));

        assertEquals("O arquivo da imagem é obrigatório", exception.getMessage());
    }
}