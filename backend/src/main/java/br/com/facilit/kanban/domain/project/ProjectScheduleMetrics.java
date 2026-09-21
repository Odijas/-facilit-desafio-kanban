package br.com.facilit.kanban.domain.project;

import java.util.Objects;

public record ProjectScheduleMetrics(
        ProjectStatus status,
        long delayDays,
        int remainingTimePercentage) {

    public ProjectScheduleMetrics {
        Objects.requireNonNull(status, "status is required");

        if (delayDays < 0) {
            throw new IllegalArgumentException("delayDays must not be negative");
        }

        if (remainingTimePercentage < 0 || remainingTimePercentage > 100) {
            throw new IllegalArgumentException("remainingTimePercentage must be between 0 and 100");
        }
    }
}
