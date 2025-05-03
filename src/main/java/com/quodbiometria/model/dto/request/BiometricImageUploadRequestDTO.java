package com.quodbiometria.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricImageUploadRequestDTO {

    @NotBlank(message = "ID do usuário é obrigatório")
    private String usuarioId;

    @NotBlank(message = "Tipo da imagem é obrigatório")
    private String tipoImagem;

    private String dispositivo;
}