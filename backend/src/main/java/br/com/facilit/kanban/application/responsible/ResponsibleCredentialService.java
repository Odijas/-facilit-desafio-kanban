package br.com.facilit.kanban.application.responsible;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.BusinessLog;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class ResponsibleCredentialService {

    static final String LOGIN_EMAIL_CONFLICT = "Este e-mail já é usado por outro login.";
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
                    "E-mail com mais de " + MAX_LOGIN_EMAIL_LENGTH + " caracteres não pode ser usado como login.");
        }
        if (credentialRepository.loginEmailTakenByAnotherUser(responsible.email(), responsible.id())) {
            throw new ConflictException(LOGIN_EMAIL_CONFLICT);
        }
        credentialRepository.save(responsible.id(), responsible.email(), rawPassword, clock.instant());
        BusinessLog.info("credencial.definida", "responsavel=" + responsible.id() + " ator=" + actor.auditLabel());
    }

    public void revoke(UUID responsibleId, Actor actor) {
        Objects.requireNonNull(responsibleId, "responsibleId is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Responsible responsible = find(responsibleId);
        if (!credentialRepository.deleteByResponsibleId(responsible.id())) {
            throw new ResourceNotFoundException("Credencial não encontrada para o responsável: " + responsibleId);
        }
        BusinessLog.info("credencial.revogada", "responsavel=" + responsible.id() + " ator=" + actor.auditLabel());
    }

    private Responsible find(UUID responsibleId) {
        return responsibleRepository.findById(responsibleId)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado: " + responsibleId));
    }

    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Informe a senha (password).");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "A senha deve ter ao menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "A senha deve ter no máximo " + MAX_PASSWORD_BYTES + " bytes.");
        }
    }
}
