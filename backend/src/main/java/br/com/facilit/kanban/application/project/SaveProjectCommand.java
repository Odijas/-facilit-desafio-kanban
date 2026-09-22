package br.com.facilit.kanban.application.project;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record SaveProjectCommand(
        String name,
        Set<UUID> responsibleIds,
        LocalDate plannedStart,
        LocalDate plannedEnd,
        LocalDate actualStart,
        LocalDate actualEnd) {

    public SaveProjectCommand {
        Objects.requireNonNull(responsibleIds, "responsibleIds is required");
        responsibleIds = Set.copyOf(responsibleIds);
    }
}
