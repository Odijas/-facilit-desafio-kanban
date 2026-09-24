package br.com.facilit.kanban.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.support.InMemoryProjectRepository;
import br.com.facilit.kanban.application.support.InMemoryResponsibleRepository;
import br.com.facilit.kanban.application.support.MutableClock;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Status e métricas acompanham a passagem dos dias sem que o projeto seja editado.
 */
class ProjectScheduleRefresherTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    // 24/09/2026 12:00 em São Paulo.
    private static final Instant NOW = Instant.parse("2026-09-24T15:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);

    private MutableClock clock;
    private InMemoryProjectRepository projectRepository;
    private ProjectService service;
    private UUID responsibleId;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW, BUSINESS_ZONE);
        projectRepository = new InMemoryProjectRepository();
        InMemoryResponsibleRepository responsibleRepository = new InMemoryResponsibleRepository();
        responsibleId = UUID.randomUUID();
        responsibleRepository.save(new Responsible(
                responsibleId,
                "Ana Silva",
                "ana@example.com",
                "Analista",
                null,
                new AuditMetadata(NOW, NOW)));
        service = new ProjectService(
                projectRepository,
                responsibleRepository,
                new ProjectScheduleCalculator(),
                clock);
    }

    @Test
    void recalculatesInProgressProjectAsOverdueAfterPlannedEndPasses() {
        Project created = create("Portal", TODAY.minusDays(2), TODAY.plusDays(2), TODAY.minusDays(2), null);
        assertThat(created.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(created.delayDays()).isZero();
        assertThat(created.remainingTimePercentage()).isEqualTo(50);

        clock.advance(Duration.ofDays(10));

        Project refreshed = service.get(created.id());
        assertThat(refreshed.status()).isEqualTo(ProjectStatus.OVERDUE);
        assertThat(refreshed.delayDays()).isEqualTo(8);
        assertThat(refreshed.remainingTimePercentage()).isZero();
        assertThat(refreshed.audit()).isEqualTo(created.audit());
        assertThat(projectRepository.scheduleCalculatedOn(created.id())).isEqualTo(TODAY.plusDays(10));
    }

    @Test
    void recalculatesNotStartedProjectAsOverdueAfterPlannedStartPasses() {
        Project created = create("Portal", TODAY.plusDays(1), TODAY.plusDays(20), null, null);
        assertThat(created.status()).isEqualTo(ProjectStatus.NOT_STARTED);

        clock.advance(Duration.ofDays(3));

        assertThat(service.get(created.id()).status()).isEqualTo(ProjectStatus.OVERDUE);
    }

    @Test
    void listingsByStatusAndIndicatorsFollowTheCurrentDate() {
        Project created = create("Portal", TODAY.minusDays(2), TODAY.plusDays(2), TODAY.minusDays(2), null);

        clock.advance(Duration.ofDays(10));

        PageQuery page = new PageQuery(0, 20);
        assertThat(service.listByStatus(ProjectStatus.IN_PROGRESS, page).content()).isEmpty();
        assertThat(service.listByStatus(ProjectStatus.OVERDUE, page).content())
                .extracting(Project::id)
                .containsExactly(created.id());
        assertThat(service.search(new ProjectFilter(ProjectStatus.OVERDUE, null, null, null, null, null), page)
                .content())
                .extracting(Project::id)
                .containsExactly(created.id());
        ProjectIndicators indicators = service.indicators();
        assertThat(indicators.delayedProjects()).isEqualTo(1);
        assertThat(indicators.byStatus()).contains(
                new ProjectStatusIndicator(ProjectStatus.IN_PROGRESS, 0, 0),
                new ProjectStatusIndicator(ProjectStatus.OVERDUE, 1, 8));
    }

    @Test
    void doesNotTouchCompletedProjects() {
        Project completed = create("Portal", TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY);
        assertThat(completed.status()).isEqualTo(ProjectStatus.COMPLETED);

        clock.advance(Duration.ofDays(30));

        assertThat(service.refreshSchedules()).isZero();
        assertThat(service.get(completed.id())).isEqualTo(completed);
        assertThat(projectRepository.scheduleCalculatedOn(completed.id())).isEqualTo(TODAY);
    }

    @Test
    void refreshIsIdempotentWithinTheSameDay() {
        create("Portal A", TODAY.minusDays(2), TODAY.plusDays(2), TODAY.minusDays(2), null);
        create("Portal B", TODAY.plusDays(1), TODAY.plusDays(20), null, null);

        assertThat(service.refreshSchedules()).isZero();

        clock.advance(Duration.ofDays(1));

        assertThat(service.refreshSchedules()).isEqualTo(2);
        assertThat(service.refreshSchedules()).isZero();
    }

    @Test
    void refreshesMoreProjectsThanOneBatch() {
        int total = ProjectScheduleRefresher.BATCH_SIZE + 3;
        for (int index = 0; index < total; index++) {
            create("Portal " + index, TODAY.minusDays(2), TODAY.plusDays(2), TODAY.minusDays(2), null);
        }

        clock.advance(Duration.ofDays(10));

        assertThat(service.refreshSchedules()).isEqualTo(total);
        assertThat(service.listByStatus(ProjectStatus.OVERDUE, new PageQuery(0, 1)).totalElements())
                .isEqualTo(total);
    }

    @Test
    void transitionStartsFromTodaysStatus() {
        Project created = create("Portal", TODAY.minusDays(2), TODAY.plusDays(2), TODAY.minusDays(2), null);

        clock.advance(Duration.ofDays(10));

        // Hoje o projeto está Atrasado: pedir Atrasado de novo é pedir o status atual.
        assertThatThrownBy(() -> service.transition(created.id(), ProjectStatus.OVERDUE, false, Actor.admin()))
                .isInstanceOf(TransitionBlockedException.class)
                .hasMessage("O projeto já está em Atrasado.");
        Project completed = service.transition(created.id(), ProjectStatus.COMPLETED, false, Actor.admin());
        assertThat(completed.dates().actualEnd()).isEqualTo(TODAY.plusDays(10));
    }

    @Test
    void usesTheBusinessTimeZoneToDecideWhatTodayIs() {
        // 24/09 às 22:30 em São Paulo já é 25/09 em UTC; "hoje" continua sendo 24/09.
        MutableClock lateEvening = new MutableClock(Instant.parse("2026-09-25T01:30:00Z"), BUSINESS_ZONE);
        InMemoryResponsibleRepository responsibles = new InMemoryResponsibleRepository();
        responsibles.save(new Responsible(
                responsibleId, "Ana Silva", "ana@example.com", "Analista", null, new AuditMetadata(NOW, NOW)));
        ProjectService lateService = new ProjectService(
                new InMemoryProjectRepository(), responsibles, new ProjectScheduleCalculator(), lateEvening);

        Project created = lateService.create(new SaveProjectCommand(
                "Portal", Set.of(responsibleId), TODAY, TODAY.plusDays(10), null, null), Actor.admin());
        Project started = lateService.transition(created.id(), ProjectStatus.IN_PROGRESS, false, Actor.admin());

        assertThat(started.dates().actualStart()).isEqualTo(TODAY);
    }

    private Project create(
            String name,
            LocalDate plannedStart,
            LocalDate plannedEnd,
            LocalDate actualStart,
            LocalDate actualEnd) {
        return service.create(new SaveProjectCommand(
                name,
                Set.of(responsibleId),
                plannedStart,
                plannedEnd,
                actualStart,
                actualEnd), Actor.admin());
    }
}
