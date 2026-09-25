package br.com.facilit.kanban.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.common.BusinessRuleException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Tabela de transição do desafio, linha a linha: efeito automático, recálculo, bloqueio com orientação e
 * confirmação obrigatória quando a ação apaga uma data registrada (linhas 4, 11 e 12).
 */
class ProjectStatusTransitionTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);
    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private final ProjectScheduleCalculator calculator = new ProjectScheduleCalculator();
    private final ProjectStatusTransition transition = new ProjectStatusTransition(calculator);

    // Linha 1 — A iniciar → Em andamento: início realizado = hoje.

    @Test
    void line01TransitionsNotStartedToInProgressBySettingActualStartToToday() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, false);

        assertThat(result.dates().actualStart()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void line01RequiresPlannedEndToClassifyTheStartedProject() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, null, null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, false))
                .isExactlyInstanceOf(BusinessRuleException.class)
                .hasMessageStartingWith("Informe o término previsto (plannedEnd)");
    }

    // Linha 2 — A iniciar → Atrasado: erro se hoje < início previsto.

    @Test
    void line02BlocksNotStartedToOverdueBeforePlannedStart() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY.plusDays(1), TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("A iniciar → Atrasado bloqueado: não é possível marcar Atrasado antes do início previsto "
                        + "(2026-09-23). Hoje é 2026-09-22.");
    }

    @Test
    void line02BlocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("A iniciar → Atrasado bloqueado: o projeto só fica Atrasado depois que o início previsto "
                        + "(2026-09-22) passar sem início realizado, ou depois que o término previsto (2026-10-02) "
                        + "passar. Hoje é 2026-09-22.");
    }

    @Test
    void line02WithMissingPlannedDatesExplainsWhatMustBeProvidedWithoutNullText() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(null, null, null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessageContaining("informe o início previsto (plannedStart) ou o término previsto (plannedEnd)")
                .hasMessageNotContaining("null");
    }

    @Test
    void line02TreatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed() {
        Project project = staleProject(
                ProjectStatus.NOT_STARTED,
                new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("O projeto já está em Atrasado.");
    }

    // Linha 3 — A iniciar → Concluído: término realizado = hoje.

    @Test
    void line03TransitionsNotStartedToCompletedBySettingActualEndToToday() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY, false);

        assertThat(result.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    // Linha 4 — Em andamento → A iniciar: início realizado = null (apaga data registrada: exige confirmação).

    @Test
    void line04RequiresConfirmationBeforeClearingActualStart() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, false))
                .isInstanceOfSatisfying(ConfirmationRequiredException.class, exception -> {
                    assertThat(exception.currentStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
                    assertThat(exception.requestedStatus()).isEqualTo(ProjectStatus.NOT_STARTED);
                    assertThat(exception.clearedField()).isEqualTo("actualStart");
                })
                .hasMessage("Confirme Em andamento → A iniciar: o início realizado (2026-09-22) será apagado. "
                        + "Reenvie com confirm = true.");
    }

    @Test
    void line04TransitionsInProgressToNotStartedByClearingActualStartWhenConfirmed() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, true);

        assertThat(result.dates().actualStart()).isNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.NOT_STARTED);
    }

    @Test
    void line04BlocksBeforeAskingConfirmationWhenClearingActualStartWouldMakeProjectOverdue() {
        Project project = project(
                ProjectStatus.IN_PROGRESS,
                new ProjectDates(TODAY.minusDays(2), TODAY.plusDays(10), TODAY.minusDays(2), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Em andamento → A iniciar bloqueado: sem o início realizado, o projeto ficaria Atrasado "
                        + "porque o início previsto (2026-09-20) já passou. Ajuste o início previsto (plannedStart) "
                        + "para hoje (2026-09-22) ou depois.");
    }

    // Linha 5 — Em andamento → Atrasado: erro pedindo remover início realizado ou ajustar datas previstas.

    @Test
    void line05BlocksInProgressToOverdueWhenDatesDoNotRecalculateAsOverdue() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOfSatisfying(TransitionBlockedException.class, exception -> {
                    assertThat(exception.currentStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
                    assertThat(exception.requestedStatus()).isEqualTo(ProjectStatus.OVERDUE);
                })
                .hasMessage("Em andamento → Atrasado bloqueado: com as datas atuais o projeto não fica Atrasado. "
                        + "Remova o início realizado (actualStart) para voltar a não iniciado, com atraso se cabível, "
                        + "ou ajuste o início ou o término previsto (plannedStart/plannedEnd) para uma data anterior "
                        + "a hoje (2026-09-22).");
    }

    @Test
    void line05DoesNotLetStaleInProgressBypassTheInProgressToOverdueBlock() {
        Project project = staleProject(
                ProjectStatus.IN_PROGRESS,
                new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("O projeto já está em Atrasado.");
    }

    // Linha 6 — Em andamento → Concluído: término realizado = hoje.

    @Test
    void line06TransitionsInProgressToCompletedBySettingActualEndToToday() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY, false);

        assertThat(result.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    // Linha 7 — Atrasado → A iniciar: erro pedindo remover início realizado e ajustar datas previstas > hoje.

    @Test
    void line07BlocksOverdueToNotStartedWithoutRequiredDateAdjustments() {
        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(2), TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Atrasado → A iniciar bloqueado: remova o início realizado (actualStart) e ajuste o início "
                        + "e o término previstos (plannedStart/plannedEnd) para datas posteriores a hoje (2026-09-22).");
    }

    @Test
    void line07AppliesToStoredInProgressThatIsOverdueToday() {
        Project project = staleProject(
                ProjectStatus.IN_PROGRESS,
                new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, true))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessageStartingWith("Atrasado → A iniciar bloqueado:");
    }

    // Linha 8 — Atrasado → Em andamento: erro pedindo ajustar datas previstas > hoje.

    @Test
    void line08BlocksOverdueToInProgressWithoutRequiredDateAdjustments() {
        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Atrasado → Em andamento bloqueado: ajuste o início e o término previstos "
                        + "(plannedStart/plannedEnd) para datas posteriores a hoje (2026-09-22); o projeto também "
                        + "precisa de início realizado (actualStart).");
    }

    // Linha 9 — Atrasado → Concluído: término realizado = hoje.

    @Test
    void line09TransitionsOverdueToCompletedBySettingActualEndToToday() {
        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY, false);

        assertThat(result.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    // Linha 10 — Concluído → A iniciar: erro pedindo remover término realizado e ajustar datas previstas > hoje.

    @Test
    void line10BlocksCompletedToNotStartedUntilActualEndAndDatesAreAdjusted() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY, TODAY.plusDays(10), null, TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, true))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Concluído → A iniciar bloqueado: remova o término realizado (actualEnd) e ajuste o início "
                        + "e o término previstos (plannedStart/plannedEnd) para datas posteriores a hoje (2026-09-22).");
    }

    // Linha 11 — Concluído → Em andamento: término realizado = null; bloqueia se ficar Atrasado.

    @Test
    void line11RequiresConfirmationBeforeClearingActualEnd() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, false))
                .isInstanceOfSatisfying(ConfirmationRequiredException.class,
                        exception -> assertThat(exception.clearedField()).isEqualTo("actualEnd"))
                .hasMessage("Confirme Concluído → Em andamento: o término realizado (2026-09-22) será apagado. "
                        + "Reenvie com confirm = true.");
    }

    @Test
    void line11WhenRecalculatedAsNotStartedAsksForActualStart() {
        Project project = project(
                ProjectStatus.COMPLETED,
                new ProjectDates(TODAY, TODAY.plusDays(10), null, TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, true))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Concluído → Em andamento bloqueado: sem o término realizado, o projeto ficaria "
                        + "A iniciar. Informe o início realizado (actualStart) para o projeto ficar Em andamento.")
                .hasMessageNotContaining("null");
    }

    @Test
    void line11TransitionsCompletedToInProgressByClearingActualEndWhenConfirmed() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, true);

        assertThat(result.dates().actualEnd()).isNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void line11BlocksCompletedToInProgressWhenClearingActualEndWouldMakeProjectOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY, true))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Concluído → Em andamento bloqueado: sem o término realizado, o projeto ficaria Atrasado. "
                        + "Ajuste o término previsto (plannedEnd) para hoje (2026-09-22) ou depois.")
                .hasMessageNotContaining("null");
    }

    // Linha 12 — Concluído → Atrasado: término realizado = null; só se as regras classificarem Atrasado.

    @Test
    void line12RequiresConfirmationBeforeClearingActualEnd() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, false))
                .isInstanceOf(ConfirmationRequiredException.class)
                .hasMessage("Confirme Concluído → Atrasado: o término realizado (2026-09-22) será apagado. "
                        + "Reenvie com confirm = true.");
    }

    @Test
    void line12TransitionsCompletedToOverdueWhenConfirmedAndDatesClassifyAsOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.OVERDUE, TODAY, true);

        assertThat(result.dates().actualEnd()).isNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(result.schedule().delayDays()).isEqualTo(1);
    }

    @Test
    void line12BlocksCompletedToOverdueWhenClearingActualEndDoesNotRecalculateAsOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY, true))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("Concluído → Atrasado bloqueado: sem o término realizado, o projeto ficaria Em andamento, "
                        + "e não Atrasado. Só é possível quando o início previsto (sem início realizado) ou o término "
                        + "previsto já passou.");
    }

    // Pedido do status atual.

    @Test
    void rejectsTransitionToCurrentStatus() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY, false))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("O projeto já está em A iniciar.");
    }

    /**
     * Projeto coerente com hoje: o status gravado é o que as datas classificam.
     */
    private Project project(ProjectStatus expectedStatus, ProjectDates dates) {
        ProjectScheduleMetrics schedule = calculator.calculate(dates, TODAY);
        assertThat(schedule.status())
                .as("as datas do teste devem classificar o projeto como %s", expectedStatus)
                .isEqualTo(expectedStatus);
        return new Project(
                UUID.randomUUID(),
                "Portal",
                Set.of(UUID.randomUUID()),
                dates,
                schedule,
                new AuditMetadata(NOW, NOW));
    }

    /**
     * Projeto gravado em outro dia: o status gravado difere do que as datas classificam hoje.
     */
    private Project staleProject(ProjectStatus storedStatus, ProjectDates dates) {
        assertThat(calculator.calculate(dates, TODAY).status())
                .as("o status gravado deve estar desatualizado")
                .isNotEqualTo(storedStatus);
        return new Project(
                UUID.randomUUID(),
                "Portal",
                Set.of(UUID.randomUUID()),
                dates,
                new ProjectScheduleMetrics(storedStatus, 0, 0),
                new AuditMetadata(NOW, NOW));
    }
}
