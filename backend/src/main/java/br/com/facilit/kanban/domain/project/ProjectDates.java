package br.com.facilit.kanban.domain.project;

import br.com.facilit.kanban.domain.common.BusinessRuleException;
import java.time.LocalDate;
import java.util.Objects;

public record ProjectDates(
        LocalDate plannedStart,
        LocalDate plannedEnd,
        LocalDate actualStart,
        LocalDate actualEnd) {

    public ProjectDates {
        if (plannedStart != null && plannedEnd != null && plannedEnd.isBefore(plannedStart)) {
            throw new BusinessRuleException("Término previsto (plannedEnd = " + plannedEnd
                    + ") não pode ser anterior ao início previsto (plannedStart = " + plannedStart + ").");
        }

        if (actualStart != null && actualEnd != null && actualEnd.isBefore(actualStart)) {
            throw new BusinessRuleException("Término realizado (actualEnd = " + actualEnd
                    + ") não pode ser anterior ao início realizado (actualStart = " + actualStart + ").");
        }
    }

    /**
     * Datas realizadas registram fatos já ocorridos, então não podem ser posteriores a hoje.
     */
    public void requireActualDatesNotAfter(LocalDate today) {
        Objects.requireNonNull(today, "today is required");

        if (actualStart != null && actualStart.isAfter(today)) {
            throw new BusinessRuleException(
                    "Início realizado (" + actualStart + ") não pode ser posterior a hoje (" + today
                            + "): informe a data em que o projeto de fato começou ou deixe o campo vazio");
        }

        if (actualEnd != null && actualEnd.isAfter(today)) {
            throw new BusinessRuleException(
                    "Término realizado (" + actualEnd + ") não pode ser posterior a hoje (" + today
                            + "): informe a data em que o projeto de fato terminou ou deixe o campo vazio");
        }
    }
}
