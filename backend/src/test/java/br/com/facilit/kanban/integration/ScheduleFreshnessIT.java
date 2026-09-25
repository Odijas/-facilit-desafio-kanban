package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.ProjectStatusIndicator;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Status e métricas acompanham a passagem dos dias com o PostgreSQL real.
 *
 * <p>A passagem do tempo é simulada no banco: as datas do projeto e a data do último cálculo recuam
 * juntas, como se o projeto tivesse sido gravado dias atrás e não fosse editado desde então.
 */
@Testcontainers
@SpringBootTest(properties = "DB_PASSWORD=test-only")
class ScheduleFreshnessIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Test
    void readsRecalculateProjectsCalculatedOnAnEarlierDayWithoutTouchingUpdatedAt() {
        LocalDate today = LocalDate.now(clock);
        Project created = createProject(
                "Portal que venceu",
                today.minusDays(2),
                today.plusDays(2),
                today.minusDays(2),
                null);
        assertThat(created.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(calculatedOn(created.id())).isEqualTo(today);

        simulateDaysWithoutEditing(created.id(), 10);
        OffsetDateTime updatedAtBefore = updatedAt(created.id());

        Project refreshed = projectService.get(created.id());

        assertThat(refreshed.status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(refreshed.delayDays()).isEqualTo(8);
        assertThat(refreshed.remainingTimePercentage()).isZero();
        assertThat(calculatedOn(created.id())).isEqualTo(today);
        assertThat(updatedAt(created.id())).isEqualTo(updatedAtBefore);
    }

    @Test
    void listingsByStatusFiltersAndIndicatorsUseTodaysStatus() {
        LocalDate today = LocalDate.now(clock);
        Project created = createProject(
                "Portal para o quadro",
                today.minusDays(2),
                today.plusDays(2),
                today.minusDays(2),
                null);
        simulateDaysWithoutEditing(created.id(), 10);

        PageQuery page = new PageQuery(0, 100);
        assertThat(projectService.search(
                        new ProjectFilter(ProjectStatus.OVERDUE, null, null, null, null, null), page)
                .content())
                .extracting(Project::id)
                .contains(created.id());
        assertThat(projectService.search(
                        new ProjectFilter(ProjectStatus.IN_PROGRESS, null, null, null, null, null), page)
                .content())
                .extracting(Project::id)
                .doesNotContain(created.id());
        assertThat(projectService.search(
                        new ProjectFilter(ProjectStatus.OVERDUE, null, null, null, null, "quadro"), page)
                .content())
                .extracting(Project::id)
                .containsExactly(created.id());

        ProjectIndicators indicators = projectService.indicators();
        ProjectStatusIndicator overdue = indicators.byStatus().stream()
                .filter(indicator -> indicator.status() == ProjectStatus.OVERDUE)
                .findFirst()
                .orElseThrow();
        assertThat(overdue.projectCount()).isGreaterThanOrEqualTo(1);
        assertThat(overdue.averageDelayDays()).isPositive();
        assertThat(indicators.delayedProjects()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void refreshIsIdempotentAndSkipsCompletedProjects() {
        LocalDate today = LocalDate.now(clock);
        Project open = createProject(
                "Portal aberto", today.plusDays(1), today.plusDays(20), null, null);
        Project completed = createProject(
                "Portal concluído", today.minusDays(5), today.minusDays(1), today.minusDays(5), today);
        simulateDaysWithoutEditing(open.id(), 3);
        simulateDaysWithoutEditing(completed.id(), 3);

        assertThat(projectService.refreshSchedules()).isGreaterThanOrEqualTo(1);
        assertThat(projectService.refreshSchedules()).isZero();
        assertThat(calculatedOn(open.id())).isEqualTo(today);
        assertThat(calculatedOn(completed.id())).isEqualTo(today.minusDays(3));
        assertThat(projectService.get(completed.id()).status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    private Project createProject(
            String name,
            LocalDate plannedStart,
            LocalDate plannedEnd,
            LocalDate actualStart,
            LocalDate actualEnd) {
        UUID responsibleId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO responsibles (id, name, email, position, created_at, updated_at)
                VALUES (?, 'Responsável IT', ?, 'Analista', now(), now())
                """,
                responsibleId,
                "freshness-" + responsibleId + "@example.invalid");
        return projectService.create(new SaveProjectCommand(
                name,
                Set.of(responsibleId),
                plannedStart,
                plannedEnd,
                actualStart,
                actualEnd), Actor.admin());
    }

    private void simulateDaysWithoutEditing(UUID projectId, int days) {
        int updated = jdbcTemplate.update(
                """
                UPDATE projects
                SET planned_start = planned_start - ?,
                    planned_end = planned_end - ?,
                    actual_start = actual_start - ?,
                    actual_end = actual_end - ?,
                    schedule_calculated_on = schedule_calculated_on - ?
                WHERE id = ?
                """,
                days, days, days, days, days, projectId);
        assertThat(updated).isEqualTo(1);
    }

    private LocalDate calculatedOn(UUID projectId) {
        return jdbcTemplate.queryForObject(
                "SELECT schedule_calculated_on FROM projects WHERE id = ?", LocalDate.class, projectId);
    }

    private OffsetDateTime updatedAt(UUID projectId) {
        return jdbcTemplate.queryForObject(
                "SELECT updated_at FROM projects WHERE id = ?", OffsetDateTime.class, projectId);
    }
}
