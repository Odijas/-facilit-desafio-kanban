package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectFilter(
        ProjectStatus status,
        UUID secretariatId,
        UUID responsibleId,
        LocalDate plannedFrom,
        LocalDate plannedTo,
        String text) {

    public ProjectFilter {
        if (plannedFrom != null && plannedTo != null && plannedTo.isBefore(plannedFrom)) {
            throw new IllegalArgumentException("plannedTo must not be before plannedFrom");
        }
        text = text == null || text.isBlank() ? null : text.trim();
    }
}
