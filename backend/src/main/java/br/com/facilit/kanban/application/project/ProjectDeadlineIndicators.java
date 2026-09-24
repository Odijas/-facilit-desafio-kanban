package br.com.facilit.kanban.application.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record ProjectDeadlineIndicators(
        int withinDays,
        LocalDate from,
        LocalDate to,
        List<ProjectDeadlineIndicator> projects) {

    public ProjectDeadlineIndicators {
        if (withinDays < 1 || withinDays > 90) {
            throw new IllegalArgumentException("withinDays must be between 1 and 90");
        }
        Objects.requireNonNull(from, "from is required");
        Objects.requireNonNull(to, "to is required");
        Objects.requireNonNull(projects, "projects is required");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must not be before from");
        }
        projects = List.copyOf(projects);
    }
}
