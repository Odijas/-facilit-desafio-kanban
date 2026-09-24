package br.com.facilit.kanban.application.responsible;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.support.InMemoryResponsibleCredentialRepository;
import br.com.facilit.kanban.application.support.InMemoryResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponsibleCredentialServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-23T12:00:00Z");
    private static final String VALID_PASSWORD = "senha-segura-123";
    private InMemoryResponsibleCredentialRepository credentialRepository;
    private ResponsibleCredentialService service;
    private Responsible responsible;

    @BeforeEach
    void setUp() {
        InMemoryResponsibleRepository responsibleRepository = new InMemoryResponsibleRepository();
        credentialRepository = new InMemoryResponsibleCredentialRepository();
        responsible = responsibleRepository.save(new Responsible(
                UUID.randomUUID(),
                "Ana Silva",
                "ana@example.com",
                "Analista",
                null,
                new AuditMetadata(NOW, NOW)));
        service = new ResponsibleCredentialService(
                responsibleRepository,
                credentialRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void administratorDefinesAndRevokesResponsibleCredentials() {
        service.setPassword(responsible.id(), VALID_PASSWORD, Actor.admin());

        assertThat(credentialRepository.find(responsible.id()))
                .hasValueSatisfying(stored -> {
                    assertThat(stored.loginEmail()).isEqualTo("ana@example.com");
                    assertThat(stored.timestamp()).isEqualTo(NOW);
                });

        service.revoke(responsible.id(), Actor.admin());
        assertThat(credentialRepository.existsForResponsible(responsible.id())).isFalse();
    }

    @Test
    void responsibleCannotManageCredentials() {
        Actor responsibleActor = Actor.responsible(responsible.id());

        assertThatThrownBy(() -> service.setPassword(responsible.id(), VALID_PASSWORD, responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> service.revoke(responsible.id(), responsibleActor))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(credentialRepository.existsForResponsible(responsible.id())).isFalse();
    }

    @Test
    void rejectsShortAndOversizedPasswords() {
        assertThatThrownBy(() -> service.setPassword(responsible.id(), "curta", Actor.admin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must have at least 12 characters");
        assertThatThrownBy(() -> service.setPassword(responsible.id(), "\u00e7".repeat(37), Actor.admin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must have at most 72 bytes");
    }

    @Test
    void rejectsLoginEmailAlreadyUsedByAnotherUser() {
        credentialRepository.registerOtherLogin("ANA@example.com");

        assertThatThrownBy(() -> service.setPassword(responsible.id(), VALID_PASSWORD, Actor.admin()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email is already used by another login");
    }

    @Test
    void reportsMissingResponsibleAndMissingCredentials() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.setPassword(unknown, VALID_PASSWORD, Actor.admin()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Responsible not found: " + unknown);
        assertThatThrownBy(() -> service.revoke(responsible.id(), Actor.admin()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Credentials not found for responsible: " + responsible.id());
    }
}
