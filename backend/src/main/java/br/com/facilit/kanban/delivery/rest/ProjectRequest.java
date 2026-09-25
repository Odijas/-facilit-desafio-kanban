package br.com.facilit.kanban.delivery.rest;

import static br.com.facilit.kanban.delivery.common.InputLimits.NAME_MAX_LENGTH;
import static br.com.facilit.kanban.delivery.common.InputLimits.RESPONSIBLES_MAX;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectRequest(
        @NotBlank @Size(max = NAME_MAX_LENGTH) @Schema(example = "Implantação do portal") String name,
        @NotEmpty @Size(max = RESPONSIBLES_MAX) Set<UUID> responsibleIds,
        @Schema(example = "2026-09-01") LocalDate plannedStart,
        @Schema(example = "2026-10-30") LocalDate plannedEnd,
        @Schema(example = "2026-09-02", description = "Não pode ser posterior a hoje") LocalDate actualStart,
        @Schema(example = "2026-09-18", description = "Não pode ser posterior a hoje") LocalDate actualEnd) {
}
