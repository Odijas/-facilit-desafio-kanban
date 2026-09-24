package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectDates;
import java.util.Objects;
import java.util.UUID;

/**
 * Datas de um projeto cujo status e métricas foram calculados antes de hoje.
 */
public record ProjectScheduleSnapshot(UUID id, ProjectDates dates) {

    public ProjectScheduleSnapshot {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(dates, "dates is required");
    }
}
