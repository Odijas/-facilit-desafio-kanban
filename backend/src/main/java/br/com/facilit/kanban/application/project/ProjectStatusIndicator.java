package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.util.Objects;

public record ProjectStatusIndicator(
        ProjectStatus status,
        long projectCount,
        double averageDelayDays) {

    public ProjectStatusIndicator {
        Objects.requireNonNull(status, "status is required");
        if (projectCount < 0) {
            throw new IllegalArgumentException("projectCount must not be negative");
        }
        if (averageDelayDays < 0) {
            throw new IllegalArgumentException("averageDelayDays must not be negative");
        }
    }
}
