package com.quodbiometria.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.opencv.core.CvType.CV_32F;

@Service
@Slf4j
public class FaceDetectionService {

    private final CascadeClassifier faceDetector;
    private final ResourceLoader resourceLoader;

    @Value("${quod.biometria.face-detection.confidence-threshold:0.7}")
    private float confidenceThreshold;

    @Value("${quod.biometria.face-detection.use-dnn:true}")
    private boolean useDnn;

    private Net faceNet;

    public FaceDetectionService(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;

        Resource haarResource = resourceLoader.getResource("classpath:models/haarcascade_frontalface_default.xml");
        Path tempFile = Files.createTempFile("haarcascade_", ".xml");
        Files.copy(haarResource.getInputStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        this.faceDetector = new CascadeClassifier(tempFile.toString());
        if (faceDetector.empty()) {
            throw new IOException("Não foi possível carregar o classificador Haar Cascade");
        }

        if (useDnn) {
            initDnnFaceDetector();
        }

        log.info("Serviço de detecção facial inicializado com sucesso");
    }

    private void initDnnFaceDetector() throws IOException {
        Resource prototxtResource = resourceLoader.getResource("classpath:models/deploy.prototxt");
        Resource modelResource = resourceLoader.getResource("classpath:models/res10_300x300_ssd_iter_140000.caffemodel");

        Path prototxtPath = Files.createTempFile("deploy_", ".prototxt");
        Path modelPath = Files.createTempFile("model_", ".caffemodel");

        Files.copy(prototxtResource.getInputStream(), prototxtPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(modelResource.getInputStream(), modelPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        faceNet = readNetFromCaffe(prototxtPath.toString(), modelPath.toString());

        if (faceNet.empty()) {
            throw new IOException("Não foi possível carregar o modelo DNN para detecção facial");
        }
    }

    public List<RectResult> detectFacesHaar(String imagePath) {
        Mat image = imread(imagePath);
        if (image.empty()) {
            return new ArrayList<>();
        }

        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        equalizeHist(grayImage, grayImage);

        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(
                grayImage,
                faces,
                1.1,
                3,
                0,
                new Size(30, 30),
                new Size()
        );

        List<RectResult> results = new ArrayList<>();
        for (long i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);
            results.add(new RectResult(face.x(), face.y(), face.width(), face.height(), 1.0f));
        }

        return results;
    }

    public List<RectResult> detectFacesDnn(String imagePath) {
        if (!useDnn || faceNet.empty()) {
            log.warn("Modelo DNN não disponível, usando Haar Cascade");
            return detectFacesHaar(imagePath);
        }

        Mat image = imread(imagePath);
        if (image.empty()) {
            log.error("Não foi possível carregar a imagem: {}", imagePath);
            return new ArrayList<>();
        }


        Mat inputBlob = org.bytedeco.opencv.global.opencv_dnn.blobFromImage(
                image,
                1.0,
                new Size(300, 300),
                new Scalar(104.0, 177.0, 123.0, 0.0),
                false,
                false,
                CV_32F
        );

        faceNet.setInput(inputBlob);
        Mat detections = faceNet.forward();

        int cols = image.cols();
        int rows = image.rows();

        List<RectResult> results = new ArrayList<>();
        FloatIndexer idx = detections.createIndexer();

        int numDetections = detections.size(2);

        for (int i = 0; i < numDetections; i++) {
            float confidence = idx.get(0, 0, i, 2);

            if (confidence > confidenceThreshold) {
                int x1 = (int) (idx.get(0, 0, i, 3) * cols);
                int y1 = (int) (idx.get(0, 0, i, 4) * rows);
                int x2 = (int) (idx.get(0, 0, i, 5) * cols);
                int y2 = (int) (idx.get(0, 0, i, 6) * rows);

                x1 = Math.max(0, Math.min(x1, cols - 1));
                y1 = Math.max(0, Math.min(y1, rows - 1));
                x2 = Math.max(0, Math.min(x2, cols - 1));
                y2 = Math.max(0, Math.min(y2, rows - 1));

                int width = x2 - x1;
                int height = y2 - y1;

                if (width > 0 && height > 0) {
                    results.add(new RectResult(x1, y1, width, height, confidence));
                }
            }
        }

        return results;
    }

    public List<RectResult> detectFaces(String imagePath) {
        if (useDnn) {
            return detectFacesDnn(imagePath);
        } else {
            return detectFacesHaar(imagePath);
        }
    }

    public record RectResult(int x, int y, int width, int height, float confidence) {
    }
}
