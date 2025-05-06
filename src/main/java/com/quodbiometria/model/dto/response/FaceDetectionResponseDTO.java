package com.quodbiometria.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceDetectionResponseDTO {
    private boolean faceDetected;
    private float confidence;
    private int faceCount;
    private Rectangle faceRectangle;
    private String processedImagePath;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Rectangle {
        private int x;
        private int y;
        private int width;
        private int height;
    }
}
