package com.quodbiometria.repository;

import com.quodbiometria.model.entity.FaceDetectionResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceDetectionRepository extends MongoRepository<FaceDetectionResult, String> {

    List<FaceDetectionResult> findByUsuarioId(String usuarioId);

    List<FaceDetectionResult> findByUsuarioIdAndFaceDetectedTrue(String usuarioId);

    Optional<FaceDetectionResult> findTopByUsuarioIdOrderByCreatedAtDesc(String usuarioId);

    List<FaceDetectionResult> findByConfidenceGreaterThanEqual(float minConfidence);
}
