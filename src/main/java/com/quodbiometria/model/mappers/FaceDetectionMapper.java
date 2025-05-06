package com.quodbiometria.model.mappers;

import com.quodbiometria.model.dto.request.FaceDetectionRequestDTO;
import com.quodbiometria.model.dto.response.FaceDetectionResponseDTO;
import com.quodbiometria.model.entity.FaceDetectionResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper manual para conversão entre DTOs e entidades de detecção facial.
 */
@Component
public class FaceDetectionMapper {

    public FaceDetectionResult toEntity(FaceDetectionRequestDTO request, FaceDetectionResponseDTO response) {
        FaceDetectionResult.FaceRectangle rectangle = null;

        if (response.getFaceRectangle() != null) {
            rectangle = FaceDetectionResult.FaceRectangle.builder()
                    .x(response.getFaceRectangle().getX())
                    .y(response.getFaceRectangle().getY())
                    .width(response.getFaceRectangle().getWidth())
                    .height(response.getFaceRectangle().getHeight())
                    .build();
        }

        return FaceDetectionResult.builder()
                .usuarioId(request.getUsuarioId())
                .dispositivo(request.getDispositivo())
                .faceDetected(response.isFaceDetected())
                .confidence(response.getConfidence())
                .faceCount(response.getFaceCount())
                .faceRectangle(rectangle)
                .processedImagePath(response.getProcessedImagePath())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public FaceDetectionResponseDTO toResponseDTO(FaceDetectionResult entity) {
        FaceDetectionResponseDTO.Rectangle rectangle = null;

        if (entity.getFaceRectangle() != null) {
            rectangle = new FaceDetectionResponseDTO.Rectangle(
                    entity.getFaceRectangle().getX(),
                    entity.getFaceRectangle().getY(),
                    entity.getFaceRectangle().getWidth(),
                    entity.getFaceRectangle().getHeight()
            );
        }

        return FaceDetectionResponseDTO.builder()
                .faceDetected(entity.isFaceDetected())
                .confidence(entity.getConfidence())
                .faceCount(entity.getFaceCount())
                .faceRectangle(rectangle)
                .processedImagePath(entity.getProcessedImagePath())
                .build();
    }

    public FaceDetectionResult updateEntity(FaceDetectionResult entity, FaceDetectionResponseDTO response) {
        entity.setFaceDetected(response.isFaceDetected());
        entity.setConfidence(response.getConfidence());
        entity.setFaceCount(response.getFaceCount());
        entity.setProcessedImagePath(response.getProcessedImagePath());

        if (response.getFaceRectangle() != null) {
            if (entity.getFaceRectangle() == null) {
                entity.setFaceRectangle(new FaceDetectionResult.FaceRectangle());
            }

            entity.getFaceRectangle().setX(response.getFaceRectangle().getX());
            entity.getFaceRectangle().setY(response.getFaceRectangle().getY());
            entity.getFaceRectangle().setWidth(response.getFaceRectangle().getWidth());
            entity.getFaceRectangle().setHeight(response.getFaceRectangle().getHeight());
        }

        return entity;
    }
}
