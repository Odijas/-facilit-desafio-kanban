package br.com.facilit.kanban.domain.project;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Aplica a tabela de transição do desafio: ações automáticas, recálculo do status e bloqueio com orientação.
 *
 * <p>Transições cuja ação automática apaga uma data já registrada (Em andamento → A iniciar,
 * Concluído → Em andamento e Concluído → Atrasado) só são aplicadas com confirmação explícita.
 */
public final class ProjectStatusTransition {

    private final ProjectScheduleCalculator scheduleCalculator;

    public ProjectStatusTransition(ProjectScheduleCalculator scheduleCalculator) {
        this.scheduleCalculator = Objects.requireNonNull(scheduleCalculator);
    }

    public ProjectTransitionResult transition(
            Project project,
            ProjectStatus requestedStatus,
            LocalDate today,
            boolean confirmed) {
        Objects.requireNonNull(project, "project is required");
        Objects.requireNonNull(requestedStatus, "requestedStatus is required");
        Objects.requireNonNull(today, "today is required");

        // O status gravado pode ter sido calculado em outro dia; a origem da transição é sempre o status de hoje.
        ProjectStatus currentStatus = scheduleCalculator.calculate(project.dates(), today).status();
        if (currentStatus == requestedStatus) {
            throw new TransitionBlockedException(
                    currentStatus,
                    requestedStatus,
                    "O projeto já está em " + requestedStatus.label() + ".");
        }

        validateBeforeTransition(currentStatus, requestedStatus, project.dates(), today);
        ProjectDates transitionedDates = applyAutomaticActions(
                currentStatus,
                requestedStatus,
                project.dates(),
                today);
        ProjectScheduleMetrics recalculated = scheduleCalculator.calculate(transitionedDates, today);

        if (recalculated.status() != requestedStatus) {
            throw new TransitionBlockedException(
                    currentStatus,
                    requestedStatus,
                    mismatchMessage(currentStatus, requestedStatus, recalculated.status(), project.dates(), today));
        }

        requireConfirmationWhenClearingRecordedDate(currentStatus, requestedStatus, project.dates(), confirmed);
        return new ProjectTransitionResult(transitionedDates, recalculated);
    }

    private static void validateBeforeTransition(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectDates dates,
            LocalDate today) {
        if (currentStatus == ProjectStatus.NOT_STARTED
                && requestedStatus == ProjectStatus.OVERDUE
                && dates.plannedStart() != null
                && today.isBefore(dates.plannedStart())) {
            throw new TransitionBlockedException(
                    currentStatus,
                    requestedStatus,
                    "A iniciar → Atrasado bloqueado: não é possível marcar Atrasado antes do início previsto ("
                            + dates.plannedStart() + "). Hoje é " + today + ".");
        }
    }

    private static ProjectDates applyAutomaticActions(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectDates dates,
            LocalDate today) {
        if (requestedStatus == ProjectStatus.COMPLETED
                && currentStatus != ProjectStatus.COMPLETED) {
            return withActualEnd(dates, today);
        }

        if (currentStatus == ProjectStatus.NOT_STARTED
                && requestedStatus == ProjectStatus.IN_PROGRESS) {
            return withActualStart(dates, today);
        }

        if (currentStatus == ProjectStatus.IN_PROGRESS
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            return withActualStart(dates, null);
        }

        if (currentStatus == ProjectStatus.COMPLETED
                && (requestedStatus == ProjectStatus.IN_PROGRESS
                        || requestedStatus == ProjectStatus.OVERDUE)) {
            return withActualEnd(dates, null);
        }

        return dates;
    }

    private static void requireConfirmationWhenClearingRecordedDate(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectDates dates,
            boolean confirmed) {
        if (confirmed) {
            return;
        }
        if (currentStatus == ProjectStatus.IN_PROGRESS && requestedStatus == ProjectStatus.NOT_STARTED) {
            throw new ConfirmationRequiredException(
                    currentStatus,
                    requestedStatus,
                    "actualStart",
                    "Confirme Em andamento → A iniciar: o início realizado (" + dates.actualStart()
                            + ") será apagado. Reenvie com confirm = true.");
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && (requestedStatus == ProjectStatus.IN_PROGRESS || requestedStatus == ProjectStatus.OVERDUE)) {
            throw new ConfirmationRequiredException(
                    currentStatus,
                    requestedStatus,
                    "actualEnd",
                    "Confirme Concluído → " + requestedStatus.label() + ": o término realizado ("
                            + dates.actualEnd() + ") será apagado. Reenvie com confirm = true.");
        }
    }

    private static ProjectDates withActualStart(ProjectDates dates, LocalDate actualStart) {
        return new ProjectDates(
                dates.plannedStart(),
                dates.plannedEnd(),
                actualStart,
                dates.actualEnd());
    }

    private static ProjectDates withActualEnd(ProjectDates dates, LocalDate actualEnd) {
        return new ProjectDates(
                dates.plannedStart(),
                dates.plannedEnd(),
                dates.actualStart(),
                actualEnd);
    }

    private static String mismatchMessage(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectStatus recalculatedStatus,
            ProjectDates dates,
            LocalDate today) {
        String transition = currentStatus.label() + " → " + requestedStatus.label() + " bloqueado: ";
        if (currentStatus == ProjectStatus.IN_PROGRESS
                && requestedStatus == ProjectStatus.OVERDUE) {
            return transition + "com as datas atuais o projeto não fica Atrasado. Remova o início realizado "
                    + "(actualStart) para voltar a não iniciado, com atraso se cabível, ou ajuste o início ou o "
                    + "término previsto (plannedStart/plannedEnd) para uma data anterior a hoje (" + today + ").";
        }
        if (currentStatus == ProjectStatus.NOT_STARTED
                && requestedStatus == ProjectStatus.OVERDUE) {
            if (dates.plannedStart() == null && dates.plannedEnd() == null) {
                return transition + "informe o início previsto (plannedStart) ou o término previsto (plannedEnd) "
                        + "para que a regra de atraso possa ser avaliada. Hoje é " + today + ".";
            }
            if (dates.plannedStart() == null) {
                return transition + "o projeto só fica Atrasado depois que o término previsto ("
                        + dates.plannedEnd() + ") passar, ou depois que um início previsto (plannedStart) informado "
                        + "passar sem início realizado. Hoje é " + today + ".";
            }
            if (dates.plannedEnd() == null) {
                return transition + "o projeto só fica Atrasado depois que o início previsto ("
                        + dates.plannedStart() + ") passar sem início realizado. Se o atraso for pelo prazo final, "
                        + "informe o término previsto (plannedEnd). Hoje é " + today + ".";
            }
            return transition + "o projeto só fica Atrasado depois que o início previsto (" + dates.plannedStart()
                    + ") passar sem início realizado, ou depois que o término previsto (" + dates.plannedEnd()
                    + ") passar. Hoje é " + today + ".";
        }
        if (currentStatus == ProjectStatus.IN_PROGRESS
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            if (dates.plannedStart() != null && dates.plannedStart().isBefore(today)) {
                return transition + "sem o início realizado, o projeto ficaria " + recalculatedStatus.label()
                        + " porque o início previsto (" + dates.plannedStart() + ") já passou. Ajuste o início "
                        + "previsto (plannedStart) para hoje (" + today + ") ou depois.";
            }
            if (dates.plannedEnd() != null && dates.plannedEnd().isBefore(today)) {
                return transition + "sem o início realizado, o projeto ficaria " + recalculatedStatus.label()
                        + " porque o término previsto (" + dates.plannedEnd() + ") já passou. Ajuste o término "
                        + "previsto (plannedEnd) para hoje (" + today + ") ou depois.";
            }
            return transition + "sem o início realizado, o projeto ficaria " + recalculatedStatus.label()
                    + ". Ajuste as datas previstas e tente de novo.";
        }
        if (currentStatus == ProjectStatus.OVERDUE
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            return transition + "remova o início realizado (actualStart) e ajuste o início e o término "
                    + "previstos (plannedStart/plannedEnd) para datas posteriores a hoje (" + today + ").";
        }
        if (currentStatus == ProjectStatus.OVERDUE
                && requestedStatus == ProjectStatus.IN_PROGRESS) {
            return transition + "ajuste o início e o término previstos (plannedStart/plannedEnd) para datas "
                    + "posteriores a hoje (" + today + "); o projeto também precisa de início realizado "
                    + "(actualStart).";
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            return transition + "remova o término realizado (actualEnd) e ajuste o início e o término "
                    + "previstos (plannedStart/plannedEnd) para datas posteriores a hoje (" + today + ").";
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && requestedStatus == ProjectStatus.IN_PROGRESS) {
            if (dates.actualStart() == null) {
                return transition + "sem o término realizado, o projeto ficaria " + recalculatedStatus.label()
                        + ". Informe o início realizado (actualStart) para o projeto ficar Em andamento.";
            }
            if (recalculatedStatus == ProjectStatus.OVERDUE) {
                return transition + "sem o término realizado, o projeto ficaria Atrasado. Ajuste o término "
                        + "previsto (plannedEnd) para hoje (" + today + ") ou depois.";
            }
            return transition + "sem o término realizado, o projeto ficaria " + recalculatedStatus.label()
                    + ". Ajuste as datas do projeto e tente de novo.";
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && requestedStatus == ProjectStatus.OVERDUE) {
            return transition + "sem o término realizado, o projeto ficaria " + recalculatedStatus.label()
                    + ", e não Atrasado. Só é possível quando o início previsto (sem início realizado) ou o "
                    + "término previsto já passou.";
        }
        return transition + "com as datas atuais, o projeto ficaria " + recalculatedStatus.label()
                + ". Ajuste as datas do projeto e tente de novo.";
    }
}
