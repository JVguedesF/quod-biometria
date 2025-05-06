package com.quodbiometria.service;

import com.quodbiometria.exception.ImageProcessingException;
import com.quodbiometria.model.dto.request.FaceDetectionRequestDTO;
import com.quodbiometria.model.dto.response.FaceDetectionResponseDTO;
import com.quodbiometria.model.entity.FaceDetectionResult;
import com.quodbiometria.model.mappers.FaceDetectionMapper;
import com.quodbiometria.repository.FaceDetectionRepository;
import com.quodbiometria.service.FaceDetectionService.RectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacialProcessingService {

    private final FaceDetectionService faceDetectionService;
    private final ImageValidationService imageValidationService;
    private final FaceDetectionRepository faceDetectionRepository;
    private final FaceDetectionMapper faceDetectionMapper;


    public FaceDetectionResponseDTO processFacialImage(MultipartFile file, FaceDetectionRequestDTO requestDTO)
            throws ImageProcessingException {
        try {
            Path tempFile = Files.createTempFile("face_", getFileExtension(file.getOriginalFilename()));
            file.transferTo(tempFile.toFile());

            imageValidationService.validateImage(file, "FACIAL");

            List<RectResult> faces = faceDetectionService.detectFaces(tempFile.toString());

            if (faces.isEmpty()) {
                throw new ImageProcessingException("Nenhuma face detectada na imagem");
            }

            if (faces.size() > 1) {
                throw new ImageProcessingException("Múltiplas faces detectadas na imagem. Apenas uma face é permitida.");
            }

            String processedImagePath = processDetectedFace(tempFile.toString(), faces.get(0));

            FaceDetectionResponseDTO responseDTO = FaceDetectionResponseDTO.builder()
                    .faceDetected(true)
                    .confidence(faces.get(0).confidence())
                    .faceCount(faces.size())
                    .faceRectangle(new FaceDetectionResponseDTO.Rectangle(
                            faces.get(0).x(),
                            faces.get(0).y(),
                            faces.get(0).width(),
                            faces.get(0).height()
                    ))
                    .processedImagePath(processedImagePath)
                    .build();

            if (requestDTO.getSalvarResultado() != null && requestDTO.getSalvarResultado()) {
                FaceDetectionResult entity = faceDetectionMapper.toEntity(requestDTO, responseDTO);
                faceDetectionRepository.save(entity);
                log.info("Resultado da detecção facial salvo com ID: {}", entity.getId());
            }

            return responseDTO;

        } catch (IOException e) {
            log.error("Erro ao processar imagem facial", e);
            throw new ImageProcessingException("Erro ao processar imagem: " + e.getMessage());
        }
    }

    private String processDetectedFace(String imagePath, RectResult face) throws IOException {
        Mat image = imread(imagePath);

        int margin = (int) (Math.max(face.width(), face.height()) * 0.2);

        int x = Math.max(0, face.x() - margin);
        int y = Math.max(0, face.y() - margin);
        int width = Math.min(image.cols() - x, face.width() + 2 * margin);
        int height = Math.min(image.rows() - y, face.height() + 2 * margin);

        Rect faceRect = new Rect(x, y, width, height);
        Mat faceMat = new Mat(image, faceRect);

        Mat resizedFace = new Mat();
        resize(faceMat, resizedFace, new Size(224, 224));

        String outputPath = System.getProperty("java.io.tmpdir") + "/processed_face_" + UUID.randomUUID() + ".jpg";
        imwrite(outputPath, resizedFace);

        return outputPath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return "." + filename.substring(filename.lastIndexOf(".") + 1);
    }

    public List<FaceDetectionResponseDTO> buscarResultadosPorUsuario(String usuarioId) {
        List<FaceDetectionResult> resultados = faceDetectionRepository.findByUsuarioId(usuarioId);
        return resultados.stream()
                .map(faceDetectionMapper::toResponseDTO)
                .toList();
    }

    public FaceDetectionResponseDTO buscarResultadoMaisRecente(String usuarioId) {
        return faceDetectionRepository.findTopByUsuarioIdOrderByCreatedAtDesc(usuarioId)
                .map(faceDetectionMapper::toResponseDTO)
                .orElse(null);
    }
}

