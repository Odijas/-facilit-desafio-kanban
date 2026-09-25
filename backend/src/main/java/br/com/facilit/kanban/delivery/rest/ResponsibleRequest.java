package br.com.facilit.kanban.delivery.rest;

import static br.com.facilit.kanban.delivery.common.InputLimits.EMAIL_MAX_LENGTH;
import static br.com.facilit.kanban.delivery.common.InputLimits.NAME_MAX_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ResponsibleRequest(
        @NotBlank @Size(max = NAME_MAX_LENGTH) @Schema(example = "Maria Silva") String name,
        @NotBlank @Email @Size(max = EMAIL_MAX_LENGTH) @Schema(example = "maria.silva@example.com") String email,
        @NotBlank @Size(max = NAME_MAX_LENGTH) @Schema(example = "Analista") String position,
        @Schema(example = "10000000-0000-4000-8000-000000000001") UUID secretariatId) {
}
