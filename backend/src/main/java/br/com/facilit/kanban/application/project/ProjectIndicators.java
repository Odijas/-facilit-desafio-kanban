package br.com.facilit.kanban.application.project;

import java.util.List;
import java.util.Objects;

public record ProjectIndicators(
        long totalProjects,
        long delayedProjects,
        List<ProjectStatusIndicator> byStatus) {

    public ProjectIndicators {
        if (totalProjects < 0 || delayedProjects < 0 || delayedProjects > totalProjects) {
            throw new IllegalArgumentException("Invalid project indicator totals");
        }
        Objects.requireNonNull(byStatus, "byStatus is required");
        byStatus = List.copyOf(byStatus);
    }
}
