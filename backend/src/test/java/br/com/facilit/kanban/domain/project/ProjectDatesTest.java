package br.com.facilit.kanban.domain.project;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.domain.common.BusinessRuleException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjectDatesTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);

    @Test
    void acceptsActualDatesUpToToday() {
        ProjectDates dates = new ProjectDates(TODAY.minusDays(5), TODAY.plusDays(5), TODAY.minusDays(5), TODAY);

        assertThatCode(() -> dates.requireActualDatesNotAfter(TODAY)).doesNotThrowAnyException();
    }

    @Test
    void acceptsProjectWithoutActualDates() {
        ProjectDates dates = new ProjectDates(TODAY.plusDays(1), TODAY.plusDays(5), null, null);

        assertThatCode(() -> dates.requireActualDatesNotAfter(TODAY)).doesNotThrowAnyException();
    }

    @Test
    void rejectsActualStartAfterToday() {
        ProjectDates dates = new ProjectDates(TODAY, TODAY.plusDays(5), TODAY.plusDays(1), null);

        assertThatThrownBy(() -> dates.requireActualDatesNotAfter(TODAY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Início realizado (2026-09-25) não pode ser posterior a hoje (2026-09-24): "
                        + "informe a data em que o projeto de fato começou ou deixe o campo vazio");
    }

    @Test
    void rejectsActualEndAfterToday() {
        ProjectDates dates = new ProjectDates(TODAY.minusDays(5), TODAY.plusDays(5), TODAY.minusDays(5), TODAY.plusDays(2));

        assertThatThrownBy(() -> dates.requireActualDatesNotAfter(TODAY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Término realizado (2026-09-26) não pode ser posterior a hoje (2026-09-24): "
                        + "informe a data em que o projeto de fato terminou ou deixe o campo vazio");
    }
}
