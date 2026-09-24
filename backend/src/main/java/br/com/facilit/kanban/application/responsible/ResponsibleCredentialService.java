package br.com.facilit.kanban.application.responsible;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class ResponsibleCredentialService {

    static final String LOGIN_EMAIL_CONFLICT = "Email is already used by another login";
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final int MAX_LOGIN_EMAIL_LENGTH = 320;

    private final ResponsibleRepository responsibleRepository;
    private final ResponsibleCredentialRepository credentialRepository;
    private final Clock clock;

    public ResponsibleCredentialService(
            ResponsibleRepository responsibleRepository,
            ResponsibleCredentialRepository credentialRepository,
            Clock clock) {
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.credentialRepository = Objects.requireNonNull(credentialRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public void setPassword(UUID responsibleId, String rawPassword, Actor actor) {
        Objects.requireNonNull(responsibleId, "responsibleId is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Responsible responsible = find(responsibleId);
        validatePassword(rawPassword);
        if (responsible.email().length() > MAX_LOGIN_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                    "email must have at most " + MAX_LOGIN_EMAIL_LENGTH + " characters to be used as login");
        }
        if (credentialRepository.loginEmailTakenByAnotherUser(responsible.email(), responsible.id())) {
            throw new ConflictException(LOGIN_EMAIL_CONFLICT);
        }
        credentialRepository.save(responsible.id(), responsible.email(), rawPassword, clock.instant());
    }

    public void revoke(UUID responsibleId, Actor actor) {
        Objects.requireNonNull(responsibleId, "responsibleId is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Responsible responsible = find(responsibleId);
        if (!credentialRepository.deleteByResponsibleId(responsible.id())) {
            throw new ResourceNotFoundException("Credentials not found for responsible: " + responsibleId);
        }
    }

    private Responsible find(UUID responsibleId) {
        return responsibleRepository.findById(responsibleId)
                .orElseThrow(() -> new ResourceNotFoundException("Responsible not found: " + responsibleId));
    }

    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "password must have at most " + MAX_PASSWORD_BYTES + " bytes");
        }
    }
}
