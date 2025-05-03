package com.quodbiometria.model.mappers;

import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.model.entity.BiometricImageMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BiometricImageMetadataMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "filename", source = "filename")
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "usuarioId", source = "usuarioId")
    @Mapping(target = "tipoImagem", source = "tipoImagem")
    @Mapping(target = "dispositivo", source = "dispositivo")
    @Mapping(target = "dataCriacao", source = "dataCriacao")
    @Mapping(target = "dataAtualizacao", source = "dataAtualizacao")
    @Mapping(target = "ativa", source = "ativa")
    BiometricImageMetadataResponseDTO toDTO(BiometricImageMetadata entity);

    List<BiometricImageMetadataResponseDTO> toDTOList(List<BiometricImageMetadata> entities);
}