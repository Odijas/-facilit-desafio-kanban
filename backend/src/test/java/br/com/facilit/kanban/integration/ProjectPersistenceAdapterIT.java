package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectScheduleSnapshot;
import br.com.facilit.kanban.application.project.ProjectScheduleUpdate;
import br.com.facilit.kanban.application.project.ProjectStatusIndicator;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleMetrics;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.infrastructure.persistence.project.ProjectPersistenceAdapter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Adaptador JPA de projetos contra o PostgreSQL real (migrations Flyway): gravação e leitura, consultas do
 * recálculo diário, filtros, paginação e indicadores. Cada teste roda numa transação desfeita no fim.
 */
@Testcontainers
@DataJpaTest(properties = "DB_PASSWORD=test-only")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectPersistenceAdapter.class)
class ProjectPersistenceAdapterIT {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @Autowired
    private ProjectPersistenceAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesAndReadsProjectWithResponsiblesCalculationDateAndVersion() {
        UUID responsible = responsible(null);
        Project project = project("Portal", Set.of(responsible),
                new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null), ProjectStatus.IN_PROGRESS, 0, 100);

        adapter.save(project, TODAY);
        clearPersistenceContext();

        assertThat(adapter.findById(project.id())).contains(project);
        assertThat(column(project.id(), "schedule_calculated_on", LocalDate.class)).isEqualTo(TODAY);
        assertThat(column(project.id(), "version", Long.class)).isZero();
        assertThat(adapter.existsByResponsibleId(responsible)).isTrue();
        assertThat(adapter.existsByResponsibleId(UUID.randomUUID())).isFalse();
    }

    @Test
    void findsOnlyOpenProjectsCalculatedBeforeTodayOrderedByIdAndLimited() {
        UUID responsible = responsible(null);
        Project staleA = saved("Stale A", responsible, ProjectStatus.IN_PROGRESS, TODAY.minusDays(1));
        Project staleB = saved("Stale B", responsible, ProjectStatus.NOT_STARTED, TODAY.minusDays(3));
        saved("Fresh", responsible, ProjectStatus.IN_PROGRESS, TODAY);
        saved("Completed", responsible, ProjectStatus.COMPLETED, TODAY.minusDays(9));
        clearPersistenceContext();

        // Ordem do PostgreSQL para uuid (bytes sem sinal) = ordem do texto canônico, não a de UUID.compareTo.
        List<UUID> expected = List.of(staleA.id(), staleB.id()).stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        assertThat(adapter.findStaleSchedules(TODAY, 10))
                .extracting(ProjectScheduleSnapshot::id)
                .containsExactlyElementsOf(expected);
        assertThat(adapter.findStaleSchedules(TODAY, 1))
                .extracting(ProjectScheduleSnapshot::id)
                .containsExactly(expected.get(0));
    }

    @Test
    void updatesSchedulesOnlyWhenCalculatedBeforeTheDateAndKeepsUpdatedAt() {
        UUID responsible = responsible(null);
        Project stale = saved("Portal", responsible, ProjectStatus.IN_PROGRESS, TODAY.minusDays(2));
        clearPersistenceContext();
        List<ProjectScheduleUpdate> updates = List.of(new ProjectScheduleUpdate(
                stale.id(), new ProjectScheduleMetrics(ProjectStatus.OVERDUE, 3, 0)));

        assertThat(adapter.updateSchedules(updates, TODAY)).isEqualTo(1);
        assertThat(adapter.updateSchedules(updates, TODAY)).isZero();
        clearPersistenceContext();

        Project refreshed = adapter.findById(stale.id()).orElseThrow();
        assertThat(refreshed.status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(refreshed.delayDays()).isEqualTo(3);
        assertThat(refreshed.audit()).isEqualTo(stale.audit());
        assertThat(column(stale.id(), "schedule_calculated_on", LocalDate.class)).isEqualTo(TODAY);
    }

    @Test
    void searchCombinesSecretariatResponsiblePeriodAndTextAndPaginatesByName() {
        UUID health = secretariat("Saúde");
        UUID education = secretariat("Educação");
        UUID ana = responsible(health);
        UUID bruno = responsible(education);
        Project portal = project("Portal da Saúde", Set.of(ana),
                new ProjectDates(TODAY, TODAY.plusDays(20), null, null), ProjectStatus.NOT_STARTED, 0, 100);
        Project clinic = project("Clínica digital", Set.of(ana),
                new ProjectDates(TODAY.plusDays(60), TODAY.plusDays(90), null, null), ProjectStatus.NOT_STARTED, 0, 100);
        Project school = project("Portal escolar", Set.of(bruno),
                new ProjectDates(TODAY, TODAY.plusDays(20), null, null), ProjectStatus.NOT_STARTED, 0, 100);
        List.of(portal, clinic, school).forEach(project -> adapter.save(project, TODAY));
        clearPersistenceContext();

        ProjectFilter filter = new ProjectFilter(
                ProjectStatus.NOT_STARTED, health, ana, TODAY.minusDays(1), TODAY.plusDays(30), "PORTAL");
        assertThat(adapter.search(filter, new PageQuery(0, 20)).content())
                .extracting(Project::id)
                .containsExactly(portal.id());

        var firstPage = adapter.findByStatus(ProjectStatus.NOT_STARTED, new PageQuery(0, 2));
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(firstPage.content()).extracting(Project::name)
                .containsExactly("Clínica digital", "Portal da Saúde");
    }

    @Test
    void indicatorsCountAndAverageDelayByStatus() {
        UUID responsible = responsible(null);
        adapter.save(project("Atrasado 2", Set.of(responsible),
                new ProjectDates(TODAY.minusDays(10), TODAY.minusDays(2), TODAY.minusDays(10), null),
                ProjectStatus.OVERDUE, 2, 0), TODAY);
        adapter.save(project("Atrasado 4", Set.of(responsible),
                new ProjectDates(TODAY.minusDays(10), TODAY.minusDays(4), TODAY.minusDays(10), null),
                ProjectStatus.OVERDUE, 4, 0), TODAY);
        adapter.save(project("Em andamento", Set.of(responsible),
                new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(9), TODAY.minusDays(1), null),
                ProjectStatus.IN_PROGRESS, 0, 90), TODAY);
        clearPersistenceContext();

        ProjectIndicators indicators = adapter.indicators();

        assertThat(indicators.totalProjects()).isEqualTo(3);
        assertThat(indicators.delayedProjects()).isEqualTo(2);
        assertThat(indicators.byStatus()).containsExactly(
                new ProjectStatusIndicator(ProjectStatus.NOT_STARTED, 0, 0),
                new ProjectStatusIndicator(ProjectStatus.IN_PROGRESS, 1, 0),
                new ProjectStatusIndicator(ProjectStatus.OVERDUE, 2, 3),
                new ProjectStatusIndicator(ProjectStatus.COMPLETED, 0, 0));
    }

    private Project saved(String name, UUID responsible, ProjectStatus status, LocalDate calculatedOn) {
        ProjectDates dates = status == ProjectStatus.COMPLETED
                ? new ProjectDates(TODAY.minusDays(20), TODAY.minusDays(10), TODAY.minusDays(20), TODAY.minusDays(9))
                : new ProjectDates(TODAY.minusDays(5), TODAY.plusDays(5), status == ProjectStatus.NOT_STARTED
                        ? null
                        : TODAY.minusDays(5), null);
        Project project = project(name, Set.of(responsible), dates, status, 0, 50);
        adapter.save(project, calculatedOn);
        return project;
    }

    private static Project project(
            String name,
            Set<UUID> responsibles,
            ProjectDates dates,
            ProjectStatus status,
            long delayDays,
            int remainingTimePercentage) {
        return new Project(
                UUID.randomUUID(),
                name,
                responsibles,
                dates,
                new ProjectScheduleMetrics(status, delayDays, remainingTimePercentage),
                new AuditMetadata(NOW, NOW));
    }

    private UUID secretariat(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO secretariats (id, name, created_at, updated_at) VALUES (?, ?, now(), now())", id, name);
        return id;
    }

    private UUID responsible(UUID secretariat) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO responsibles (id, name, email, position, secretariat_id, created_at, updated_at)
                VALUES (?, 'Responsável', ?, 'Analista', ?, now(), now())
                """,
                id, "adapter-" + id + "@example.invalid", secretariat);
        return id;
    }

    private <T> T column(UUID projectId, String column, Class<T> type) {
        return jdbcTemplate.queryForObject(
                "SELECT " + column + " FROM projects WHERE id = ?", type, projectId);
    }

    private void clearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
