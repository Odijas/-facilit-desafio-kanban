package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusRequest(
        @NotNull @Schema(example = "IN_PROGRESS") ProjectStatus status) {
}
