package com.quodbiometria.repository;

import com.quodbiometria.model.entity.BiometricImageMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiometricImageMetadataRepository extends MongoRepository<BiometricImageMetadata, String> {

    List<BiometricImageMetadata> findByUsuarioId(String usuarioId);

    List<BiometricImageMetadata> findByUsuarioIdAndTipoImagem(String usuarioId, String tipoImagem);

    List<BiometricImageMetadata> findByHashAndUsuarioId(String hash, String usuarioId);
}