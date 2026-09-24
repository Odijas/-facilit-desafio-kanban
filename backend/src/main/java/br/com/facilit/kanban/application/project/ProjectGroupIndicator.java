package br.com.facilit.kanban.application.project;

import java.util.Objects;
import java.util.UUID;

public record ProjectGroupIndicator(
        UUID id,
        long projectCount,
        double averageDelayDays) {

    public ProjectGroupIndicator {
        Objects.requireNonNull(id, "id is required");
        if (projectCount < 0) {
            throw new IllegalArgumentException("projectCount must not be negative");
        }
        if (averageDelayDays < 0) {
            throw new IllegalArgumentException("averageDelayDays must not be negative");
        }
    }
}
