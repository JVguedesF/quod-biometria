package com.quodbiometria.repository;

import com.quodbiometria.model.entity.BiometricImageMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiometricImageMetadataRepository extends MongoRepository<BiometricImageMetadata, String> {

    List<BiometricImageMetadata> findByUsuarioId(String usuarioId);

    List<BiometricImageMetadata> findByUsuarioIdAndTipoImagem(String usuarioId, String tipoImagem);

    Optional<BiometricImageMetadata> findByFileId(String fileId);

    void deleteByFileId(String fileId);
}