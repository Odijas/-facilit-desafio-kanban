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
        @Schema(example = "2026-09-22") LocalDate plannedStart,
        @Schema(example = "2026-10-10") LocalDate plannedEnd,
        @Schema(example = "2026-09-22") LocalDate actualStart,
        @Schema(example = "2026-10-09") LocalDate actualEnd) {
}
