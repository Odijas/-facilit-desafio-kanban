package br.com.facilit.kanban.delivery.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ResponsibleRequest(
        @NotBlank @Schema(example = "Maria Silva") String name,
        @NotBlank @Email @Schema(example = "maria.silva@example.com") String email,
        @NotBlank @Schema(example = "Analista") String position,
        @Schema(example = "10000000-0000-4000-8000-000000000001") UUID secretariatId) {
}
