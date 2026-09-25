package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.delivery.common.ApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResponsibleCredentialsRequest(
        @NotBlank
        @Schema(
                description = "Senha de acesso: mínimo de 12 caracteres e no máximo 72 bytes em UTF-8",
                example = ApiExamples.PASSWORD,
                accessMode = Schema.AccessMode.WRITE_ONLY)
        String password) {
}
