package br.com.facilit.kanban.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjectFilterTest {

    @Test
    void trimsTextAndAcceptsTheMaximumLength() {
        ProjectFilter filter = new ProjectFilter(null, null, null, null, null, "  " + "a".repeat(100) + "  ");

        assertThat(filter.text()).hasSize(ProjectFilter.TEXT_MAX_LENGTH);
        assertThat(new ProjectFilter(null, null, null, null, null, "   ").text()).isNull();
    }

    @Test
    void rejectsTextAboveTheMaximumLength() {
        assertThatThrownBy(() -> new ProjectFilter(null, null, null, null, null, "a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Texto de busca muito longo: use no máximo 100 caracteres (text).");
    }

    @Test
    void rejectsPeriodEndingBeforeItStarts() {
        assertThatThrownBy(() -> new ProjectFilter(
                        null, null, null, LocalDate.of(2026, 9, 24), LocalDate.of(2026, 9, 23), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Período inválido: plannedTo não pode ser anterior a plannedFrom.");
    }
}
