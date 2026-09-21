package br.com.facilit.kanban.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void exposesCalculatedScheduleMetrics() {
        Project project = project(new ProjectScheduleMetrics(ProjectStatus.OVERDUE, 2, 0));

        assertThat(project.status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(project.delayDays()).isEqualTo(2);
        assertThat(project.remainingTimePercentage()).isZero();
    }

    @Test
    void rejectsMissingScheduleMetrics() {
        assertThatThrownBy(() -> project(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("schedule is required");
    }

    @Test
    void scheduleMetricsRejectInvalidRanges() {
        assertThatThrownBy(() -> new ProjectScheduleMetrics(ProjectStatus.OVERDUE, -1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("delayDays must not be negative");
        assertThatThrownBy(() -> new ProjectScheduleMetrics(ProjectStatus.NOT_STARTED, 0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("remainingTimePercentage must be between 0 and 100");
    }

    private static Project project(ProjectScheduleMetrics schedule) {
        Instant now = Instant.parse("2026-09-21T22:00:00Z");
        return new Project(
                UUID.randomUUID(),
                "Portal",
                Set.of(UUID.randomUUID()),
                new ProjectDates(
                        LocalDate.of(2026, 9, 22),
                        LocalDate.of(2026, 9, 30),
                        null,
                        null),
                schedule,
                new AuditMetadata(now, now));
    }
}
