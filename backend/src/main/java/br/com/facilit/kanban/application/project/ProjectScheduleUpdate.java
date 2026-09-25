package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectScheduleMetrics;
import java.util.Objects;
import java.util.UUID;

/**
 * Status e métricas recalculados de um projeto.
 */
public record ProjectScheduleUpdate(UUID id, ProjectScheduleMetrics schedule) {

    public ProjectScheduleUpdate {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(schedule, "schedule is required");
    }
}
