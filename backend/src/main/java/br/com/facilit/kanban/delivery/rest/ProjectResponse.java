package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.delivery.common.ApiExamples;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/** Projeto com status e métricas calculados para hoje. O exemplo é um projeto concluído antes do prazo. */
public record ProjectResponse(
        @Schema(example = ApiExamples.PROJECT_ID) UUID id,
        @Schema(example = ApiExamples.PROJECT_NAME) String name,
        @Schema(example = "COMPLETED", description = "Status calculado para hoje") ProjectStatus status,
        Set<UUID> responsibleIds,
        @Schema(example = "2026-09-01") LocalDate plannedStart,
        @Schema(example = "2026-10-30") LocalDate plannedEnd,
        @Schema(example = "2026-09-02") LocalDate actualStart,
        @Schema(example = "2026-09-18") LocalDate actualEnd,
        @Schema(example = "0", description = "Dias de atraso calculados para hoje") long delayDays,
        @Schema(example = "0", description = "Percentual de tempo restante até o término previsto (0 a 100)")
        int remainingTimePercentage,
        @Schema(example = ApiExamples.CREATED_AT) Instant createdAt,
        @Schema(example = ApiExamples.UPDATED_AT) Instant updatedAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.id(),
                project.name(),
                project.status(),
                project.responsibleIds(),
                project.dates().plannedStart(),
                project.dates().plannedEnd(),
                project.dates().actualStart(),
                project.dates().actualEnd(),
                project.delayDays(),
                project.remainingTimePercentage(),
                project.audit().createdAt(),
                project.audit().updatedAt());
    }
}
