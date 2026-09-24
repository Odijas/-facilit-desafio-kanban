package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record ProjectDeadlineIndicator(
        UUID projectId,
        String projectName,
        ProjectStatus status,
        LocalDate plannedEnd,
        long daysUntilDeadline) {

    public ProjectDeadlineIndicator {
        Objects.requireNonNull(projectId, "projectId is required");
        requireText(projectName, "projectName");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(plannedEnd, "plannedEnd is required");
        if (daysUntilDeadline < 0) {
            throw new IllegalArgumentException("daysUntilDeadline must not be negative");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
