package com.quodbiometria.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.quodbiometria.model.dto.request.BiometricImageUploadRequestDTO;
import com.quodbiometria.model.dto.response.BiometricImageMetadataResponseDTO;
import com.quodbiometria.model.entity.BiometricImageMetadata;
import com.quodbiometria.model.mappers.BiometricImageMetadataMapper;
import com.quodbiometria.repository.BiometricImageMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricImageStorageService {

    private final GridFsTemplate gridFsTemplate;
    private final BiometricImageMetadataRepository metadataRepository;
    private final BiometricImageMetadataMapper mapper;

    public BiometricImageMetadataResponseDTO storeImage(MultipartFile file, BiometricImageUploadRequestDTO requestDTO,
                                                        Map<String, String> additionalMetadata) {
        try {
            String hash = calculateSHA256(file.getInputStream());

            // Criar documento de metadados para o GridFS
            Document metadataDoc = new Document();
            metadataDoc.append("usuarioId", requestDTO.getUsuarioId());
            metadataDoc.append("tipoImagem", requestDTO.getTipoImagem());
            metadataDoc.append("hash", hash);

            if (additionalMetadata != null) {
                additionalMetadata.forEach(metadataDoc::append);
            }

            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    metadataDoc
            );

            String dispositivo = requestDTO.getDispositivo() != null ?
                    requestDTO.getDispositivo() : "DESCONHECIDO";

            BiometricImageMetadata imageMetadata = BiometricImageMetadata.builder()
                    .fileId(fileId.toString())
                    .filename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .usuarioId(requestDTO.getUsuarioId())
                    .tipoImagem(requestDTO.getTipoImagem())
                    .dispositivo(dispositivo)
                    .dataCriacao(LocalDateTime.now())
                    .dataAtualizacao(LocalDateTime.now())
                    .hash(hash)
                    .ativa(true)
                    .build();

            BiometricImageMetadata savedMetadata = metadataRepository.save(imageMetadata);
            return mapper.toDTO(savedMetadata);

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Erro ao armazenar imagem biométrica", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao armazenar imagem biométrica: " + e.getMessage());
        }
    }

    public byte[] getImage(String id) {
        try {
            BiometricImageMetadata metadata = findMetadataById(id);
            return getImageByFileId(metadata.getFileId());
        } catch (IOException e) {
            log.error("Erro ao recuperar imagem biométrica", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao recuperar imagem biométrica: " + e.getMessage());
        }
    }

    public byte[] getImageByFileId(String fileId) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
        GridFsResource resource = gridFsTemplate.getResource(file);
        return IOUtils.toByteArray(resource.getInputStream());
    }


    public BiometricImageMetadataResponseDTO getMetadataById(String id) {
        return mapper.toDTO(findMetadataById(id));
    }

    public List<BiometricImageMetadataResponseDTO> getImagesByUsuario(String usuarioId) {
        List<BiometricImageMetadata> images = metadataRepository.findByUsuarioId(usuarioId);
        return mapper.toDTOList(images);
    }

    public List<BiometricImageMetadataResponseDTO> getImagesByUsuarioAndTipo(String usuarioId, String tipoImagem) {
        List<BiometricImageMetadata> images = metadataRepository.findByUsuarioIdAndTipoImagem(usuarioId, tipoImagem);
        return mapper.toDTOList(images);
    }

    public void deleteImage(String id) {
        BiometricImageMetadata metadata = findMetadataById(id);
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(metadata.getFileId()))));
        metadataRepository.delete(metadata);
    }

    private BiometricImageMetadata findMetadataById(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Imagem biométrica não encontrada"));
    }

    private String calculateSHA256(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hash = digest.digest();

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}