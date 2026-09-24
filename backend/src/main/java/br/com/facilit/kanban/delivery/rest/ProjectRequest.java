package br.com.facilit.kanban.delivery.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectRequest(
        @NotBlank @Schema(example = "Implantação do portal") String name,
        @NotEmpty Set<UUID> responsibleIds,
        @Schema(example = "2026-09-01") LocalDate plannedStart,
        @Schema(example = "2026-10-30") LocalDate plannedEnd,
        @Schema(example = "2026-09-02", description = "Não pode ser posterior a hoje") LocalDate actualStart,
        @Schema(example = "2026-09-18", description = "Não pode ser posterior a hoje") LocalDate actualEnd) {
}
