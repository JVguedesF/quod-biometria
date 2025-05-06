package com.quodbiometria.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ImageMetadataExtractionService {

    public Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();

        try {
            processImageMetadata(file, metadata);
            return metadata;
        } catch (IOException | ImageReadException e) {
            log.warn("Erro ao extrair metadados EXIF: {}", e.getMessage());
            log.debug("Detalhes do erro:", e);
            addBasicMetadata(metadata, file);
            metadata.put("error", "Não foi possível extrair metadados EXIF: " + e.getMessage());
            return metadata;
        }
    }

    private void processImageMetadata(MultipartFile file, Map<String, String> metadata) throws IOException, ImageReadException {
        ImageMetadata imageMetadata = Imaging.getMetadata(file.getInputStream(), file.getOriginalFilename());

        if (imageMetadata instanceof JpegImageMetadata jpegMetadata) {
            processJpegMetadata(metadata, jpegMetadata);
        }

        if (metadata.isEmpty()) {
            log.info("Nenhum metadado EXIF encontrado na imagem");
        }

        addBasicMetadata(metadata, file);
    }

    private void processJpegMetadata(Map<String, String> metadata, JpegImageMetadata jpegMetadata) {
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE, "camera.fabricante");
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL, "camera.modelo");
        addTagToMetadata(metadata, jpegMetadata, ExifTagConstants.EXIF_TAG_SOFTWARE, "software");
        addTagToMetadata(metadata, jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, "data.original");
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION, "resolucao.x");
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_YRESOLUTION, "resolucao.y");
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_ORIENTATION, "orientacao");
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_IMAGE_WIDTH, "largura");
        addTagToMetadata(metadata, jpegMetadata, TiffTagConstants.TIFF_TAG_IMAGE_LENGTH, "altura");

        TiffImageMetadata exifMetadata = jpegMetadata.getExif();
        if (exifMetadata != null) {
            extractGpsInfo(metadata, exifMetadata);
        }
    }

    private void addBasicMetadata(Map<String, String> metadata, MultipartFile file) {
        metadata.put("filename", file.getOriginalFilename());
        metadata.put("contentType", file.getContentType());
        metadata.put("size", String.valueOf(file.getSize()));
        metadata.put("extractedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
    }

    private void extractGpsInfo(Map<String, String> metadata, TiffImageMetadata exifMetadata) {
        try {
            TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
            if (gpsInfo != null) {
                double longitude = gpsInfo.getLongitudeAsDegreesEast();
                double latitude = gpsInfo.getLatitudeAsDegreesNorth();
                metadata.put("gps.latitude", String.valueOf(latitude));
                metadata.put("gps.longitude", String.valueOf(longitude));
            }
        } catch (ImageReadException e) {
            log.debug("Não foi possível extrair informações de GPS", e);
        }
    }

    private void addTagToMetadata(Map<String, String> metadata, JpegImageMetadata jpegMetadata,
                                  TagInfo tagInfo, String metadataKey) {
        try {
            TiffField field = jpegMetadata.findEXIFValue(tagInfo);
            if (field != null) {
                String value = field.getValueDescription();
                if (value != null && !value.isEmpty()) {
                    value = value.replaceAll("(^\")|(\"|$)", "");
                    metadata.put(metadataKey, value);
                }
            }
        } catch (Exception e) {
            log.debug("Não foi possível ler a tag {}: {}", tagInfo.name, e.getMessage());
        }
    }

    public Map<String, String> sanitizeMetadata(Map<String, String> metadata) {
        Map<String, String> sanitized = new HashMap<>();
        final String tipoImagem = metadata.getOrDefault("tipoImagem", "");

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("gps.") && isPersonalImageType(tipoImagem)) {
                continue;
            }

            if (value != null) {
                if (value.length() > 255) {
                    sanitized.put(key, value.substring(0, 252) + "...");
                } else {
                    sanitized.put(key, value);
                }
            }
        }

        return sanitized;
    }

    private boolean isPersonalImageType(String tipoImagem) {
        return tipoImagem.equalsIgnoreCase("FACIAL") ||
                tipoImagem.equalsIgnoreCase("DIGITAL");
    }
}