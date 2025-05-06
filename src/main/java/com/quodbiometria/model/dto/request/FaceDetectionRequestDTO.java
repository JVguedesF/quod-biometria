package com.quodbiometria.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceDetectionRequestDTO {
    @NotNull(message = "O ID do usuário é obrigatório")
    private String usuarioId;

    private String dispositivo;

    private Boolean salvarResultado;

    @Builder.Default
    private Boolean normalizarFace = true;
}
