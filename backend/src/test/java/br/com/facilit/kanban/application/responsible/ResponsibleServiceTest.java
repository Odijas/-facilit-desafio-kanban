package br.com.facilit.kanban.application.responsible;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.support.InMemoryProjectRepository;
import br.com.facilit.kanban.application.support.InMemoryResponsibleRepository;
import br.com.facilit.kanban.application.support.InMemorySecretariatRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleMetrics;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponsibleServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private InMemoryResponsibleRepository responsibleRepository;
    private InMemoryProjectRepository projectRepository;
    private InMemorySecretariatRepository secretariatRepository;
    private ResponsibleService service;

    @BeforeEach
    void setUp() {
        responsibleRepository = new InMemoryResponsibleRepository();
        projectRepository = new InMemoryProjectRepository();
        secretariatRepository = new InMemorySecretariatRepository();
        service = new ResponsibleService(
                responsibleRepository,
                secretariatRepository,
                projectRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsReadsListsUpdatesAndDeletesResponsible() {
        Responsible created = service.create(command("Ana@Example.com", null));

        assertThat(created.email()).isEqualTo("ana@example.com");
        assertThat(created.audit().createdAt()).isEqualTo(NOW);
        assertThat(service.get(created.id())).isEqualTo(created);
        assertThat(service.list(new PageQuery(0, 20)).content()).containsExactly(created);

        Responsible updated = service.update(
                created.id(),
                new SaveResponsibleCommand("Ana Souza", "ANA@EXAMPLE.COM", "Gestora", null));
        assertThat(updated.name()).isEqualTo("Ana Souza");
        assertThat(updated.audit().createdAt()).isEqualTo(created.audit().createdAt());

        service.delete(created.id());
        assertThatThrownBy(() -> service.get(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.create(command("ana@example.com", null));

        assertThatThrownBy(() -> service.create(command("ANA@EXAMPLE.COM", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Responsible email already exists");
    }

    @Test
    void rejectsUnknownSecretariat() {
        UUID secretariatId = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(command("ana@example.com", secretariatId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Secretariat not found: " + secretariatId);
    }

    @Test
    void blocksDeleteWhenResponsibleIsAssignedToProject() {
        Responsible responsible = service.create(command("ana@example.com", null));
        Instant now = NOW;
        projectRepository.save(new Project(
                UUID.randomUUID(),
                "Portal",
                Set.of(responsible.id()),
                new ProjectDates(null, null, null, null),
                new ProjectScheduleMetrics(ProjectStatus.NOT_STARTED, 0, 0),
                new AuditMetadata(now, now)));

        assertThatThrownBy(() -> service.delete(responsible.id()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Responsible is assigned to at least one project");
    }

    private static SaveResponsibleCommand command(String email, UUID secretariatId) {
        return new SaveResponsibleCommand("Ana Silva", email, "Analista", secretariatId);
    }
}
