package br.com.facilit.kanban.domain.project;

import java.util.Objects;

public record ProjectTransitionResult(
        ProjectDates dates,
        ProjectScheduleMetrics schedule) {

    public ProjectTransitionResult {
        Objects.requireNonNull(dates, "dates is required");
        Objects.requireNonNull(schedule, "schedule is required");
    }
}
