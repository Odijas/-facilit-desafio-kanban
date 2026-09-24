package br.com.facilit.kanban.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjectStatusTransitionTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);
    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private final ProjectScheduleCalculator calculator = new ProjectScheduleCalculator();
    private final ProjectStatusTransition transition = new ProjectStatusTransition(calculator);

    @Test
    void transitionsNotStartedToInProgressBySettingActualStartToToday() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY);

        assertThat(result.dates().actualStart()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void blocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match recalculated status NOT_STARTED");
    }

    @Test
    void treatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed() {
        Project project = staleProject(
                ProjectStatus.NOT_STARTED,
                new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already in status OVERDUE");
    }

    @Test
    void blocksNotStartedToOverdueBeforePlannedStart() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY.plusDays(1), TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before plannedStart");
    }

    @Test
    void transitionsNotStartedToCompletedBySettingActualEndToToday() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY);

        assertThat(result.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void transitionsInProgressToNotStartedByClearingActualStart() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.NOT_STARTED, TODAY);

        assertThat(result.dates().actualStart()).isNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.NOT_STARTED);
    }

    @Test
    void doesNotLetStaleInProgressBypassTheInProgressToOverdueBlock() {
        Project project = staleProject(
                ProjectStatus.IN_PROGRESS,
                new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already in status OVERDUE");
    }

    @Test
    void appliesOverdueRowWhenStoredInProgressIsStale() {
        Project project = staleProject(
                ProjectStatus.IN_PROGRESS,
                new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transition OVERDUE to NOT_STARTED");
    }

    @Test
    void blocksInProgressToOverdueWhenDatesDoNotRecalculateAsOverdue() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("remove actualStart");
    }

    @Test
    void transitionsInProgressToCompletedBySettingActualEndToToday() {
        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY);

        assertThat(result.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void blocksOverdueToNotStartedWithoutRequiredDateAdjustments() {
        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(2), TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("adjust planned dates");
    }

    @Test
    void blocksOverdueToInProgressWithoutRequiredDateAdjustments() {
        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("adjust plannedEnd");
    }

    @Test
    void transitionsOverdueToCompletedBySettingActualEndToToday() {
        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY);

        assertThat(result.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void blocksCompletedToNotStartedUntilActualEndAndDatesAreAdjusted() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY, TODAY.plusDays(10), null, TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("remove actualEnd");
    }

    @Test
    void transitionsCompletedToInProgressByClearingActualEndWhenResultIsNotOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY);

        assertThat(result.dates().actualEnd()).isNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void blocksCompletedToInProgressWhenClearingActualEndWouldMakeProjectOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be overdue");
    }

    @Test
    void transitionsCompletedToOverdueOnlyWhenClearingActualEndRecalculatesAsOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));

        ProjectTransitionResult result = transition.transition(project, ProjectStatus.OVERDUE, TODAY);

        assertThat(result.dates().actualEnd()).isNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.OVERDUE);
    }

    @Test
    void blocksCompletedToOverdueWhenClearingActualEndDoesNotRecalculateAsOverdue() {
        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must satisfy the overdue date rules");
    }

    @Test
    void rejectsTransitionToCurrentStatus() {
        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));

        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in status");
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
