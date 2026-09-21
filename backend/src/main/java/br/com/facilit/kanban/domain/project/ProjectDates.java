package br.com.facilit.kanban.domain.project;

import java.time.LocalDate;

public record ProjectDates(
        LocalDate plannedStart,
        LocalDate plannedEnd,
        LocalDate actualStart,
        LocalDate actualEnd) {

    public ProjectDates {
        if (plannedStart != null && plannedEnd != null && plannedEnd.isBefore(plannedStart)) {
            throw new IllegalArgumentException("plannedEnd must not be before plannedStart");
        }

        if (actualStart != null && actualEnd != null && actualEnd.isBefore(actualStart)) {
            throw new IllegalArgumentException("actualEnd must not be before actualStart");
        }
    }
}
