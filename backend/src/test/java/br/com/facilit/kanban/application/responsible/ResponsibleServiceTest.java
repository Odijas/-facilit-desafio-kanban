package br.com.facilit.kanban.application.responsible;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.support.InMemoryProjectRepository;
import br.com.facilit.kanban.application.support.InMemoryResponsibleCredentialRepository;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponsibleServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);
    private InMemoryResponsibleRepository responsibleRepository;
    private InMemoryProjectRepository projectRepository;
    private InMemorySecretariatRepository secretariatRepository;
    private InMemoryResponsibleCredentialRepository credentialRepository;
    private ResponsibleService service;

    @BeforeEach
    void setUp() {
        responsibleRepository = new InMemoryResponsibleRepository();
        projectRepository = new InMemoryProjectRepository();
        secretariatRepository = new InMemorySecretariatRepository();
        credentialRepository = new InMemoryResponsibleCredentialRepository();
        service = new ResponsibleService(
                responsibleRepository,
                secretariatRepository,
                projectRepository,
                credentialRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsReadsListsUpdatesAndDeletesResponsible() {
        Responsible created = service.create(command("Ana@Example.com", null), Actor.admin());

        assertThat(created.email()).isEqualTo("ana@example.com");
        assertThat(created.audit().createdAt()).isEqualTo(NOW);
        assertThat(service.get(created.id())).isEqualTo(created);
        assertThat(service.list(new PageQuery(0, 20)).content()).containsExactly(created);

        Responsible updated = service.update(
                created.id(),
                new SaveResponsibleCommand("Ana Souza", "ANA@EXAMPLE.COM", "Gestora", null),
                Actor.admin());
        assertThat(updated.name()).isEqualTo("Ana Souza");
        assertThat(updated.audit().createdAt()).isEqualTo(created.audit().createdAt());

        service.delete(created.id(), Actor.admin());
        assertThatThrownBy(() -> service.get(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.create(command("ana@example.com", null), Actor.admin());

        assertThatThrownBy(() -> service.create(command("ANA@EXAMPLE.COM", null), Actor.admin()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe responsável com este e-mail.");
    }

    @Test
    void rejectsUnknownSecretariat() {
        UUID secretariatId = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(command("ana@example.com", secretariatId), Actor.admin()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Secretaria não encontrada: " + secretariatId);
    }

    @Test
    void blocksDeleteWhenResponsibleIsAssignedToProject() {
        Responsible responsible = service.create(command("ana@example.com", null), Actor.admin());
        Instant now = NOW;
        projectRepository.save(new Project(
                UUID.randomUUID(),
                "Portal",
                Set.of(responsible.id()),
                new ProjectDates(null, null, null, null),
                new ProjectScheduleMetrics(ProjectStatus.NOT_STARTED, 0, 0),
                new AuditMetadata(now, now)),
                TODAY);

        assertThatThrownBy(() -> service.delete(responsible.id(), Actor.admin()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("O responsável está vinculado a ao menos um projeto e não pode ser excluído.");
    }

    @Test
    void onlyAdministratorManagesResponsibles() {
        Responsible created = service.create(command("ana@example.com", null), Actor.admin());
        Actor responsibleActor = Actor.responsible(created.id());

        assertThatThrownBy(() -> service.create(command("bruno@example.com", null), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Apenas o administrador pode realizar esta operação.");
        assertThatThrownBy(() -> service.update(created.id(), command("outro@example.com", null), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> service.delete(created.id(), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(service.get(created.id()).email()).isEqualTo("ana@example.com");
    }

    @Test
    void blocksEmailChangeThatCollidesWithAnotherLogin() {
        Responsible created = service.create(command("ana@example.com", null), Actor.admin());
        credentialRepository.save(created.id(), created.email(), "senha-segura-123", NOW);
        credentialRepository.registerOtherLogin("admin@example.com");

        assertThatThrownBy(() -> service.update(created.id(), command("admin@example.com", null), Actor.admin()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Este e-mail já é usado por outro login.");
        assertThat(service.update(created.id(), command("ana.silva@example.com", null), Actor.admin()).email())
                .isEqualTo("ana.silva@example.com");
    }

    private static SaveResponsibleCommand command(String email, UUID secretariatId) {
        return new SaveResponsibleCommand("Ana Silva", email, "Analista", secretariatId);
    }
}
