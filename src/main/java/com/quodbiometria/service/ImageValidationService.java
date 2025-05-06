package com.quodbiometria.service;

import com.quodbiometria.exception.ImageValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ImageValidationService {

    @Value("${app.image.validation.max-size:5242880}")
    private long maxFileSize;

    @Value("${app.image.validation.min-width:100}")
    private int minWidth;

    @Value("${app.image.validation.min-height:100}")
    private int minHeight;

    @Value("${app.image.validation.allowed-formats:jpeg,jpg,png}")
    private String[] allowedFormats;


    public void validateImage(MultipartFile file, String tipoImagem) throws ImageValidationException {
        if (file == null || file.isEmpty()) {
            throw new ImageValidationException("O arquivo da imagem é obrigatório");
        }

        validateFileSize(file);

        validateFileFormat(file);

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new ImageValidationException("Não foi possível ler a imagem. Formato inválido ou arquivo corrompido.");
            }

            validateDimensions(image, tipoImagem);

            if ("FACIAL".equalsIgnoreCase(tipoImagem)) {
                validateFacialImage(image);
            } else if ("DIGITAL".equalsIgnoreCase(tipoImagem)) {
                validateFingerprintImage(image);
            } else if ("DOCUMENTO".equalsIgnoreCase(tipoImagem)) {
                validateDocumentImage(image);
            }

        } catch (IOException e) {
            log.error("Erro ao ler imagem para validação", e);
            throw new ImageValidationException("Erro ao processar a imagem: " + e.getMessage());
        }
    }

    private void validateFileSize(MultipartFile file) throws ImageValidationException {
        if (file.getSize() > maxFileSize) {
            throw new ImageValidationException(
                    String.format("O tamanho do arquivo excede o limite máximo permitido de %d bytes", maxFileSize));
        }
    }

    private void validateFileFormat(MultipartFile file) throws ImageValidationException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ImageValidationException("Nome do arquivo não fornecido");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedFormats);

        if (!allowedExtensions.contains(fileExtension)) {
            throw new ImageValidationException(
                    String.format("Formato de arquivo não permitido. Formatos aceitos: %s",
                            String.join(", ", allowedFormats)));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageValidationException("O arquivo enviado não é uma imagem válida");
        }
    }

    private void validateDimensions(BufferedImage image, String tipoImagem) throws ImageValidationException {
        int width = image.getWidth();
        int height = image.getHeight();

        int requiredMinWidth = minWidth;
        int requiredMinHeight = minHeight;

        if ("FACIAL".equalsIgnoreCase(tipoImagem)) {
            requiredMinWidth = 300;
            requiredMinHeight = 300;
        } else if ("DIGITAL".equalsIgnoreCase(tipoImagem)) {
            requiredMinWidth = 200;
            requiredMinHeight = 200;
        } else if ("DOCUMENTO".equalsIgnoreCase(tipoImagem)) {
            requiredMinWidth = 800;
            requiredMinHeight = 600;
        }

        if (width < requiredMinWidth || height < requiredMinHeight) {
            throw new ImageValidationException(
                    String.format("A imagem não atende às dimensões mínimas requeridas para %s: %dx%d pixels",
                            tipoImagem, requiredMinWidth, requiredMinHeight));
        }
    }

    private void validateFacialImage(BufferedImage image) throws ImageValidationException {
        double aspectRatio = (double) image.getWidth() / image.getHeight();
        if (aspectRatio < 0.7 || aspectRatio > 1.5) {
            throw new ImageValidationException(
                    "A proporção da imagem facial está fora do padrão aceitável (deve ser aproximadamente quadrada)");
        }
    }

    private void validateFingerprintImage(BufferedImage image) throws ImageValidationException {
        double aspectRatio = (double) image.getWidth() / image.getHeight();
        if (aspectRatio < 0.8 || aspectRatio > 1.25) {
            throw new ImageValidationException(
                    "A proporção da imagem da impressão digital está fora do padrão aceitável");
        }
    }

    private void validateDocumentImage(BufferedImage image) throws ImageValidationException {
        double aspectRatio = (double) image.getWidth() / image.getHeight();
        if (aspectRatio < 1.2 || aspectRatio > 1.8) {
            throw new ImageValidationException(
                    "A proporção da imagem do documento está fora do padrão aceitável (deve ser retangular)");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}