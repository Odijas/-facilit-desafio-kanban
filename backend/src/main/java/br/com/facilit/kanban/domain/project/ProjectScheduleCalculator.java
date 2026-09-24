package br.com.facilit.kanban.domain.project;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class ProjectScheduleCalculator {

    public ProjectScheduleMetrics calculate(ProjectDates dates, LocalDate today) {
        Objects.requireNonNull(dates, "dates is required");
        Objects.requireNonNull(today, "today is required");

        ProjectStatus status = calculateStatus(dates, today);
        long delayDays = calculateDelayDays(dates, status, today);
        int remainingTimePercentage = calculateRemainingTimePercentage(dates, status, today);

        return new ProjectScheduleMetrics(status, delayDays, remainingTimePercentage);
    }

    private ProjectStatus calculateStatus(ProjectDates dates, LocalDate today) {
        if (dates.actualEnd() != null) {
            return ProjectStatus.COMPLETED;
        }

        boolean missedStart = dates.plannedStart() != null
                && dates.plannedStart().isBefore(today)
                && dates.actualStart() == null;
        boolean missedEnd = dates.plannedEnd() != null && dates.plannedEnd().isBefore(today);

        if (missedStart || missedEnd) {
            return ProjectStatus.OVERDUE;
        }

        if (dates.actualStart() != null) {
            if (dates.plannedEnd() == null) {
                throw new IllegalArgumentException(
                        "plannedEnd is required when actualStart is filled and actualEnd is empty");
            }
            return ProjectStatus.IN_PROGRESS;
        }

        return ProjectStatus.NOT_STARTED;
    }

    private long calculateDelayDays(ProjectDates dates, ProjectStatus status, LocalDate today) {
        if (status == ProjectStatus.COMPLETED
                || dates.plannedEnd() == null
                || !dates.plannedEnd().isBefore(today)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(dates.plannedEnd(), today);
    }

    private int calculateRemainingTimePercentage(
            ProjectDates dates,
            ProjectStatus status,
            LocalDate today) {
        if (status == ProjectStatus.COMPLETED
                || dates.plannedStart() == null
                || dates.plannedEnd() == null) {
            return 0;
        }

        long totalDays = ChronoUnit.DAYS.between(dates.plannedStart(), dates.plannedEnd());
        if (totalDays <= 0) {
            return 0;
        }

        long usedDays = ChronoUnit.DAYS.between(dates.plannedStart(), today);
        long remainingDays = totalDays - usedDays;

        if (remainingDays <= 0) {
            return 0;
        }
        if (remainingDays >= totalDays) {
            return 100;
        }

        return (int) Math.round(remainingDays * 100.0d / totalDays);
    }
}
