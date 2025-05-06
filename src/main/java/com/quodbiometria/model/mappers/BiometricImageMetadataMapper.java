package com.quodbiometria.model.mappers;

import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.model.entity.BiometricImageMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BiometricImageMetadataMapper {

    public BiometricImageMetadataResponseDTO toDTO(BiometricImageMetadata entity) {
        if (entity == null) {
            return null;
        }

        return BiometricImageMetadataResponseDTO.builder()
                .id(entity.getId())
                .filename(entity.getFilename())
                .contentType(entity.getContentType())
                .size(entity.getSize())
                .usuarioId(entity.getUsuarioId())
                .tipoImagem(entity.getTipoImagem())
                .dispositivo(entity.getDispositivo())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .ativa(entity.isAtiva())
                .exifMetadata(entity.getExifMetadata())
                .build();
    }

    public List<BiometricImageMetadataResponseDTO> toDTOList(List<BiometricImageMetadata> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public BiometricImageMetadata toEntity(BiometricImageMetadataResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        return BiometricImageMetadata.builder()
                .id(dto.getId())
                .filename(dto.getFilename())
                .contentType(dto.getContentType())
                .size(dto.getSize())
                .usuarioId(dto.getUsuarioId())
                .tipoImagem(dto.getTipoImagem())
                .dispositivo(dto.getDispositivo())
                .dataCriacao(dto.getDataCriacao())
                .dataAtualizacao(dto.getDataAtualizacao())
                .ativa(dto.isAtiva())
                .exifMetadata(dto.getExifMetadata())
                .build();
    }
}