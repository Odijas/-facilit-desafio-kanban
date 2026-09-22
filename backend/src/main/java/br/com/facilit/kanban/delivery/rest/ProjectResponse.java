package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        ProjectStatus status,
        Set<UUID> responsibleIds,
        LocalDate plannedStart,
        LocalDate plannedEnd,
        LocalDate actualStart,
        LocalDate actualEnd,
        long delayDays,
        int remainingTimePercentage,
        Instant createdAt,
        Instant updatedAt) {

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
