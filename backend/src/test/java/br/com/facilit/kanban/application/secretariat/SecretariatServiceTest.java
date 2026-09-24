package br.com.facilit.kanban.application.secretariat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.support.DirectTransactionRunner;
import br.com.facilit.kanban.application.support.InMemoryResponsibleRepository;
import br.com.facilit.kanban.application.support.InMemorySecretariatRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecretariatServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private InMemorySecretariatRepository secretariatRepository;
    private InMemoryResponsibleRepository responsibleRepository;
    private SecretariatService service;

    @BeforeEach
    void setUp() {
        secretariatRepository = new InMemorySecretariatRepository();
        responsibleRepository = new InMemoryResponsibleRepository();
        service = new SecretariatService(
                secretariatRepository,
                responsibleRepository,
                new DirectTransactionRunner(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsReadsListsUpdatesAndDeletesSecretariat() {
        var created = service.create(new SaveSecretariatCommand("Secretaria de Tecnologia"), Actor.admin());

        assertThat(created.audit().createdAt()).isEqualTo(NOW);
        assertThat(service.get(created.id())).isEqualTo(created);
        assertThat(service.list(new PageQuery(0, 20)).content()).containsExactly(created);

        var updated = service.update(created.id(), new SaveSecretariatCommand("Secretaria Digital"), Actor.admin());
        assertThat(updated.name()).isEqualTo("Secretaria Digital");
        assertThat(updated.audit().createdAt()).isEqualTo(created.audit().createdAt());

        service.delete(created.id(), Actor.admin());
        assertThatThrownBy(() -> service.get(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void blocksDeleteWhenSecretariatIsAssignedToResponsible() {
        var secretariat = service.create(new SaveSecretariatCommand("Secretaria de Gestão"), Actor.admin());
        responsibleRepository.save(new Responsible(
                UUID.randomUUID(),
                "Ana Silva",
                "ana@example.com",
                "Gestora",
                secretariat.id(),
                new AuditMetadata(NOW, NOW)));

        assertThatThrownBy(() -> service.delete(secretariat.id(), Actor.admin()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("A secretaria tem responsáveis vinculados e não pode ser excluída.");
    }

    @Test
    void onlyAdministratorManagesSecretariats() {
        var secretariat = service.create(new SaveSecretariatCommand("Secretaria de Obras"), Actor.admin());
        Actor responsibleActor = Actor.responsible(UUID.randomUUID());

        assertThatThrownBy(() -> service.create(new SaveSecretariatCommand("Nova"), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> service.update(secretariat.id(), new SaveSecretariatCommand("Nova"), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> service.delete(secretariat.id(), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(service.get(secretariat.id()).name()).isEqualTo("Secretaria de Obras");
    }
}
