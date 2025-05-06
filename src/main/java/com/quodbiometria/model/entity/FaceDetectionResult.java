package com.quodbiometria.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "face_detection_results")
public class FaceDetectionResult {

    @Id
    private String id;

    private String usuarioId;

    private String imageId;

    private boolean faceDetected;

    private float confidence;

    private int faceCount;

    private FaceRectangle faceRectangle;

    private String processedImagePath;

    private String dispositivo;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FaceRectangle {
        private int x;
        private int y;
        private int width;
        private int height;
    }
}
