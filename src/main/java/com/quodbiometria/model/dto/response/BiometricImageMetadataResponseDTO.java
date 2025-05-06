package com.quodbiometria.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricImageMetadataResponseDTO {

    private String id;
    private String filename;
    private String contentType;
    private long size;
    private String usuarioId;
    private String tipoImagem;
    private String dispositivo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private boolean ativa;
    private Map<String, String> exifMetadata;
}