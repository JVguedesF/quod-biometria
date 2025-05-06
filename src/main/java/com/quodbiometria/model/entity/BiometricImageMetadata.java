package com.quodbiometria.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "biometric_images_metadata")
public class BiometricImageMetadata {

    @Id
    private String id;

    private String fileId;
    private String filename;
    private String contentType;
    private long size;
    private String usuarioId;
    private String tipoImagem;
    private String dispositivo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private String hash;
    private boolean ativa;
    private Map<String, String> exifMetadata;
}