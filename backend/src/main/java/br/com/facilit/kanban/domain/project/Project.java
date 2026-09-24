package br.com.facilit.kanban.domain.project;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        Set<UUID> responsibleIds,
        ProjectDates dates,
        ProjectScheduleMetrics schedule,
        AuditMetadata audit) {

    public Project {
        Objects.requireNonNull(id, "id is required");
        requireText(name, "name");
        Objects.requireNonNull(responsibleIds, "responsibleIds is required");
        Objects.requireNonNull(dates, "dates is required");
        Objects.requireNonNull(schedule, "schedule is required");
        Objects.requireNonNull(audit, "audit is required");

        if (responsibleIds.isEmpty()) {
            throw new IllegalArgumentException("responsibleIds must contain at least one responsible");
        }

        responsibleIds = Set.copyOf(responsibleIds);
    }

    public ProjectStatus status() {
        return schedule.status();
    }

    public long delayDays() {
        return schedule.delayDays();
    }

    public int remainingTimePercentage() {
        return schedule.remainingTimePercentage();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
