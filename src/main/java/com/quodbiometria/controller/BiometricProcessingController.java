package com.quodbiometria.controller;

import com.quodbiometria.exception.ImageProcessingException;
import com.quodbiometria.model.dto.request.FaceDetectionRequestDTO;
import com.quodbiometria.model.dto.response.ApiResponseDTO;
import com.quodbiometria.model.dto.response.FaceDetectionResponseDTO;
import com.quodbiometria.service.FacialProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/biometrics/processing")
@RequiredArgsConstructor
@Slf4j
public class BiometricProcessingController {

    private final FacialProcessingService facialProcessingService;

    /**
     * Endpoint para detecção facial em uma imagem
     *
     * @param file Arquivo de imagem a ser processado
     * @param usuarioId ID do usuário
     * @param dispositivo Informações do dispositivo (opcional)
     * @param salvarResultado Flag para salvar o resultado (opcional)
     * @return Resultado da detecção facial
     */
    @PostMapping(value = "/face-detection", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<FaceDetectionResponseDTO>> detectFace(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(value = "dispositivo", required = false) String dispositivo,
            @RequestParam(value = "salvarResultado", required = false) Boolean salvarResultado) {

        try {
            log.info("Recebida solicitação para detecção facial para usuário: {}", usuarioId);

            FaceDetectionRequestDTO requestDTO = FaceDetectionRequestDTO.builder()
                    .usuarioId(usuarioId)
                    .dispositivo(dispositivo)
                    .salvarResultado(salvarResultado != null ? salvarResultado : false)
                    .build();

            FaceDetectionResponseDTO result = facialProcessingService.processFacialImage(file, requestDTO);

            return ResponseEntity.ok(new ApiResponseDTO<>(
                    true,
                    "Detecção facial realizada com sucesso",
                    result
            ));
        } catch (ImageProcessingException e) {
            log.error("Erro na detecção facial: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(
                            false,
                            "Erro na detecção facial: " + e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("Erro interno ao processar detecção facial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(
                            false,
                            "Erro interno ao processar detecção facial",
                            null
                    ));
        }
    }

    /**
     * Endpoint para buscar resultados de detecção facial por usuário
     *
     * @param usuarioId ID do usuário
     * @return Lista de resultados de detecção facial
     */
    @GetMapping("/face-detection/usuario/{usuarioId}")
    public ResponseEntity<ApiResponseDTO<List<FaceDetectionResponseDTO>>> buscarResultadosPorUsuario(
            @PathVariable String usuarioId) {

        try {
            List<FaceDetectionResponseDTO> resultados = facialProcessingService.buscarResultadosPorUsuario(usuarioId);

            if (resultados.isEmpty()) {
                return ResponseEntity.ok(new ApiResponseDTO<>(
                        true,
                        "Nenhum resultado encontrado para o usuário",
                        resultados
                ));
            }

            return ResponseEntity.ok(new ApiResponseDTO<>(
                    true,
                    "Resultados encontrados com sucesso",
                    resultados
            ));
        } catch (Exception e) {
            log.error("Erro ao buscar resultados de detecção facial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(
                            false,
                            "Erro ao buscar resultados: " + e.getMessage(),
                            null
                    ));
        }
    }

    /**
     * Endpoint para buscar o resultado mais recente de detecção facial por usuário
     *
     * @param usuarioId ID do usuário
     * @return Resultado mais recente de detecção facial
     */
    @GetMapping("/face-detection/usuario/{usuarioId}/recente")
    public ResponseEntity<ApiResponseDTO<FaceDetectionResponseDTO>> buscarResultadoMaisRecente(
            @PathVariable String usuarioId) {

        try {
            FaceDetectionResponseDTO resultado = facialProcessingService.buscarResultadoMaisRecente(usuarioId);

            if (resultado == null) {
                return ResponseEntity.ok(new ApiResponseDTO<>(
                        true,
                        "Nenhum resultado encontrado para o usuário",
                        null
                ));
            }

            return ResponseEntity.ok(new ApiResponseDTO<>(
                    true,
                    "Resultado mais recente encontrado com sucesso",
                    resultado
            ));
        } catch (Exception e) {
            log.error("Erro ao buscar resultado mais recente de detecção facial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(
                            false,
                            "Erro ao buscar resultado: " + e.getMessage(),
                            null
                    ));
        }
    }
}
