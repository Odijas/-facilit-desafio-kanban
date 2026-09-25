package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros da busca de projetos. O texto é limitado a {@value #TEXT_MAX_LENGTH} caracteres depois de aparado.
 */
public record ProjectFilter(
        ProjectStatus status,
        UUID secretariatId,
        UUID responsibleId,
        LocalDate plannedFrom,
        LocalDate plannedTo,
        String text) {

    public static final int TEXT_MAX_LENGTH = 100;

    public ProjectFilter {
        if (plannedFrom != null && plannedTo != null && plannedTo.isBefore(plannedFrom)) {
            throw new IllegalArgumentException("Período inválido: plannedTo não pode ser anterior a plannedFrom.");
        }
        text = text == null || text.isBlank() ? null : text.trim();
        if (text != null && text.length() > TEXT_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Texto de busca muito longo: use no máximo " + TEXT_MAX_LENGTH + " caracteres (text).");
        }
    }
}
