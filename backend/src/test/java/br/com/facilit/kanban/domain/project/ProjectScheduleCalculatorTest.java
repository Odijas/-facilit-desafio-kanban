package br.com.facilit.kanban.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.domain.common.BusinessRuleException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjectScheduleCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 21);
    private final ProjectScheduleCalculator calculator = new ProjectScheduleCalculator();

    @Test
    void classifiesProjectWithoutActualDatesAsNotStarted() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY, TODAY.plusDays(10), null, null), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.NOT_STARTED);
        assertThat(metrics.delayDays()).isZero();
        assertThat(metrics.remainingTimePercentage()).isEqualTo(100);
    }

    @Test
    void classifiesMissedPlannedStartAsOverdue() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY.minusDays(2), TODAY.plusDays(8), null, null), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(metrics.delayDays()).isZero();
    }

    @Test
    void classifiesMissedPlannedEndAsOverdueAndCalculatesDelay() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY.minusDays(10), TODAY.minusDays(3), TODAY.minusDays(9), null), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(metrics.delayDays()).isEqualTo(3);
        assertThat(metrics.remainingTimePercentage()).isZero();
    }

    @Test
    void classifiesStartedProjectAsInProgress() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY.minusDays(5), TODAY.plusDays(5), TODAY.minusDays(4), null), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(metrics.remainingTimePercentage()).isEqualTo(50);
    }

    @Test
    void keepsProjectInProgressOnItsPlannedEndDate() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY.minusDays(10), TODAY, TODAY.minusDays(9), null), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(metrics.delayDays()).isZero();
        assertThat(metrics.remainingTimePercentage()).isZero();
    }

    @Test
    void classifiesProjectWithActualEndAsCompleted() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY.minusDays(10), TODAY.minusDays(2), null, TODAY.minusDays(1)), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.COMPLETED);
        assertThat(metrics.delayDays()).isZero();
        assertThat(metrics.remainingTimePercentage()).isZero();
    }

    @Test
    void returnsZeroRemainingPercentageWhenPlannedDatesAreMissing() {
        ProjectScheduleMetrics metrics = calculator.calculate(dates(null, null, null, null), TODAY);

        assertThat(metrics.status()).isEqualTo(ProjectStatus.NOT_STARTED);
        assertThat(metrics.remainingTimePercentage()).isZero();
    }

    @Test
    void returnsZeroRemainingPercentageWhenTotalDurationIsZero() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY, TODAY, null, null), TODAY);

        assertThat(metrics.remainingTimePercentage()).isZero();
    }

    @Test
    void capsRemainingPercentageAtOneHundredBeforePlannedStart() {
        ProjectScheduleMetrics metrics = calculator.calculate(
                dates(TODAY.plusDays(5), TODAY.plusDays(15), null, null), TODAY);

        assertThat(metrics.remainingTimePercentage()).isEqualTo(100);
    }

    @Test
    void rejectsStartedProjectWithoutPlannedEndWhenItCannotBeClassified() {
        ProjectDates dates = dates(TODAY.minusDays(1), null, TODAY, null);

        assertThatThrownBy(() -> calculator.calculate(dates, TODAY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Informe o término previsto (plannedEnd): projeto com início realizado e sem término "
                        + "realizado precisa dele para ser classificado como Em andamento ou Atrasado.");
    }

    @Test
    void rejectsPlannedEndBeforePlannedStart() {
        assertThatThrownBy(() -> dates(TODAY, TODAY.minusDays(1), null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageStartingWith("Término previsto (plannedEnd = ")
                .hasMessageContaining("não pode ser anterior ao início previsto (plannedStart = ");
    }

    @Test
    void rejectsActualEndBeforeActualStart() {
        assertThatThrownBy(() -> dates(TODAY, TODAY.plusDays(10), TODAY, TODAY.minusDays(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageStartingWith("Término realizado (actualEnd = ")
                .hasMessageContaining("não pode ser anterior ao início realizado (actualStart = ");
    }

    private static ProjectDates dates(
            LocalDate plannedStart,
            LocalDate plannedEnd,
            LocalDate actualStart,
            LocalDate actualEnd) {
        return new ProjectDates(plannedStart, plannedEnd, actualStart, actualEnd);
    }
}
