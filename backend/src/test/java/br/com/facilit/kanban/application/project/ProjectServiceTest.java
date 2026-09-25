package br.com.facilit.kanban.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.support.CapturedBusinessLog;
import br.com.facilit.kanban.application.support.DirectTransactionRunner;
import br.com.facilit.kanban.application.support.InMemoryProjectRepository;
import br.com.facilit.kanban.application.support.InMemoryResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.common.BusinessRuleException;
import br.com.facilit.kanban.domain.project.ConfirmationRequiredException;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);
    private InMemoryProjectRepository projectRepository;
    private InMemoryResponsibleRepository responsibleRepository;
    private ProjectService service;
    private UUID responsibleId;

    @BeforeEach
    void setUp() {
        projectRepository = new InMemoryProjectRepository();
        responsibleRepository = new InMemoryResponsibleRepository();
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
                new DirectTransactionRunner(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsReadsListsUpdatesAndDeletesProjectWithRecalculatedMetrics() {
        Project created = service.create(command(null), Actor.admin());

        assertThat(created.status()).isEqualTo(ProjectStatus.NOT_STARTED);
        assertThat(created.remainingTimePercentage()).isEqualTo(100);
        assertThat(service.get(created.id())).isEqualTo(created);
        assertThat(service.list(new PageQuery(0, 20)).content()).containsExactly(created);
        assertThat(service.listByStatus(ProjectStatus.NOT_STARTED, new PageQuery(0, 20)).content())
                .containsExactly(created);
        assertThat(service.listByStatus(ProjectStatus.IN_PROGRESS, new PageQuery(0, 20)).content())
                .isEmpty();

        Project updated = service.update(created.id(), command(TODAY), Actor.admin());
        assertThat(updated.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(updated.audit().createdAt()).isEqualTo(created.audit().createdAt());

        Project completed = service.transition(created.id(), ProjectStatus.COMPLETED, false, Actor.admin());
        assertThat(completed.status()).isEqualTo(ProjectStatus.COMPLETED);
        assertThat(completed.dates().actualEnd()).isEqualTo(TODAY);
        assertThat(completed.audit().createdAt()).isEqualTo(created.audit().createdAt());

        service.delete(created.id(), Actor.admin());
        assertThatThrownBy(() -> service.get(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchesProjectsAndBuildsIndicators() {
        Project matching = service.create(new SaveProjectCommand(
                "Portal do cidadão",
                Set.of(responsibleId),
                TODAY,
                TODAY.plusDays(10),
                null,
                null), Actor.admin());
        projectRepository.assignResponsibleToSecretariat(
                responsibleId,
                UUID.fromString("10000000-0000-4000-8000-000000000001"));

        var result = service.search(
                new ProjectFilter(
                        ProjectStatus.NOT_STARTED,
                        UUID.fromString("10000000-0000-4000-8000-000000000001"),
                        responsibleId,
                        TODAY.minusDays(1),
                        TODAY.plusDays(20),
                        "  cidadão  "),
                new PageQuery(0, 20));

        assertThat(result.content()).containsExactly(matching);
        ProjectIndicators indicators = service.indicators();
        assertThat(indicators.totalProjects()).isEqualTo(1);
        assertThat(indicators.delayedProjects()).isZero();
        assertThat(indicators.byStatus()).containsExactly(
                new ProjectStatusIndicator(ProjectStatus.NOT_STARTED, 1, 0),
                new ProjectStatusIndicator(ProjectStatus.IN_PROGRESS, 0, 0),
                new ProjectStatusIndicator(ProjectStatus.OVERDUE, 0, 0),
                new ProjectStatusIndicator(ProjectStatus.COMPLETED, 0, 0));
    }

    @Test
    void rejectsInvertedAdvancedFilterPeriod() {
        assertThatThrownBy(() -> new ProjectFilter(
                        null,
                        null,
                        null,
                        TODAY.plusDays(1),
                        TODAY,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Período inválido: plannedTo não pode ser anterior a plannedFrom.");
    }

    @Test
    void rejectsActualDatesAfterTodayOnCreateAndUpdate() {
        SaveProjectCommand futureStart = new SaveProjectCommand(
                "Portal",
                Set.of(responsibleId),
                TODAY,
                TODAY.plusDays(10),
                TODAY.plusDays(1),
                null);

        assertThatThrownBy(() -> service.create(futureStart, Actor.admin()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageStartingWith("Início realizado (2026-09-23) não pode ser posterior a hoje (2026-09-22)");

        Project created = service.create(command(null), Actor.admin());
        SaveProjectCommand futureEnd = new SaveProjectCommand(
                "Portal",
                Set.of(responsibleId),
                TODAY,
                TODAY.plusDays(10),
                TODAY,
                TODAY.plusDays(1));

        assertThatThrownBy(() -> service.update(created.id(), futureEnd, Actor.admin()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageStartingWith("Término realizado (2026-09-23) não pode ser posterior a hoje (2026-09-22)");
        assertThat(service.get(created.id())).isEqualTo(created);
    }

    @Test
    void clearsRecordedActualStartOnlyWithExplicitConfirmation() {
        Project started = service.create(command(TODAY), Actor.admin());
        assertThat(started.status()).isEqualTo(ProjectStatus.IN_PROGRESS);

        assertThatThrownBy(() -> service.transition(started.id(), ProjectStatus.NOT_STARTED, false, Actor.admin()))
                .isInstanceOf(ConfirmationRequiredException.class);
        assertThat(service.get(started.id())).isEqualTo(started);

        Project reverted = service.transition(started.id(), ProjectStatus.NOT_STARTED, true, Actor.admin());
        assertThat(reverted.status()).isEqualTo(ProjectStatus.NOT_STARTED);
        assertThat(reverted.dates().actualStart()).isNull();
    }

    @Test
    void recordsBusinessEventsWithIdentifiersOnlyAndNoPersonalData() {
        try (CapturedBusinessLog log = CapturedBusinessLog.start()) {
            Project created = service.create(command(null), Actor.admin());
            service.transition(created.id(), ProjectStatus.IN_PROGRESS, false, Actor.admin());
            assertThatThrownBy(() -> service.transition(created.id(), ProjectStatus.OVERDUE, false, Actor.admin()))
                    .isInstanceOf(TransitionBlockedException.class);
            service.delete(created.id(), Actor.responsible(responsibleId));

            assertThat(log.messages()).containsExactly(
                    "evento=projeto.criado id=" + created.id() + " status=NOT_STARTED ator=ADMIN",
                    "evento=projeto.transicao id=" + created.id()
                            + " de=NOT_STARTED para=IN_PROGRESS confirmado=false ator=ADMIN",
                    "evento=projeto.transicao.recusada id=" + created.id()
                            + " de=IN_PROGRESS para=OVERDUE motivo=TransitionBlockedException ator=ADMIN",
                    "evento=projeto.excluido id=" + created.id() + " ator=RESPONSIBLE:" + responsibleId);
            assertThat(String.join("\n", log.messages()))
                    .doesNotContain("ana@example.com")
                    .doesNotContain("Ana Silva");
        }
    }

    @Test
    void rejectsProjectWithUnknownResponsible() {
        SaveProjectCommand command = new SaveProjectCommand(
                "Portal",
                Set.of(UUID.randomUUID()),
                TODAY,
                TODAY.plusDays(10),
                null,
                null);

        assertThatThrownBy(() -> service.create(command, Actor.admin()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ao menos um responsável informado não foi encontrado.");
    }

    @Test
    void responsibleManagesOnlyProjectsAssignedToThem() {
        Actor owner = Actor.responsible(responsibleId);
        Project owned = service.create(command(null), owner);

        Project transitioned = service.transition(owned.id(), ProjectStatus.IN_PROGRESS, false, owner);
        assertThat(transitioned.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(service.update(owned.id(), command(TODAY), owner).name()).isEqualTo("Portal");

        Actor outsider = Actor.responsible(UUID.randomUUID());
        assertThatThrownBy(() -> service.update(owned.id(), command(TODAY), outsider))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("O responsável só pode alterar projetos em que é responsável.");
        assertThatThrownBy(() -> service.transition(owned.id(), ProjectStatus.COMPLETED, false, outsider))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> service.delete(owned.id(), outsider))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(service.get(owned.id()).status()).isEqualTo(ProjectStatus.IN_PROGRESS);

        service.delete(owned.id(), owner);
        assertThatThrownBy(() -> service.get(owned.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void responsibleMustRemainAssignedWhenCreatingOrUpdating() {
        Actor outsider = Actor.responsible(UUID.randomUUID());

        assertThatThrownBy(() -> service.create(command(null), outsider))
                .isInstanceOf(ForbiddenOperationException.class);

        Actor owner = Actor.responsible(responsibleId);
        Project owned = service.create(command(null), owner);
        UUID otherResponsibleId = UUID.randomUUID();
        responsibleRepository.save(new Responsible(
                otherResponsibleId,
                "Bruno Lima",
                "bruno@example.com",
                "Gestor",
                null,
                new AuditMetadata(NOW, NOW)));
        SaveProjectCommand withoutOwner = new SaveProjectCommand(
                "Portal",
                Set.of(otherResponsibleId),
                TODAY,
                TODAY.plusDays(10),
                null,
                null);

        assertThatThrownBy(() -> service.update(owned.id(), withoutOwner, owner))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(service.update(owned.id(), withoutOwner, Actor.admin()).responsibleIds())
                .containsExactly(otherResponsibleId);
    }

    private SaveProjectCommand command(LocalDate actualStart) {
        return new SaveProjectCommand(
                "Portal",
                Set.of(responsibleId),
                TODAY,
                TODAY.plusDays(10),
                actualStart,
                null);
    }
}
