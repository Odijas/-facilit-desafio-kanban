package br.com.facilit.kanban.application.responsible;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.BusinessLog;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.common.TransactionRunner;
import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ResponsibleService {

    private final ResponsibleRepository responsibleRepository;
    private final SecretariatRepository secretariatRepository;
    private final ProjectRepository projectRepository;
    private final ResponsibleCredentialRepository credentialRepository;
    private final TransactionRunner transactions;
    private final Clock clock;

    public ResponsibleService(
            ResponsibleRepository responsibleRepository,
            SecretariatRepository secretariatRepository,
            ProjectRepository projectRepository,
            ResponsibleCredentialRepository credentialRepository,
            TransactionRunner transactions,
            Clock clock) {
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.secretariatRepository = Objects.requireNonNull(secretariatRepository);
        this.projectRepository = Objects.requireNonNull(projectRepository);
        this.credentialRepository = Objects.requireNonNull(credentialRepository);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
    }

    public Responsible create(SaveResponsibleCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Responsible saved = transactions.execute(() -> {
            Instant now = clock.instant();
            Responsible responsible = new Responsible(
                    UUID.randomUUID(),
                    command.name(),
                    command.email(),
                    command.position(),
                    command.secretariatId(),
                    new AuditMetadata(now, now));

            validateSecretariat(responsible.secretariatId());
            ensureEmailAvailable(responsible.email(), null);
            return responsibleRepository.save(responsible);
        });
        BusinessLog.info("responsavel.criado", "id=" + saved.id() + " ator=" + actor.auditLabel());
        return saved;
    }

    public Responsible get(UUID id) {
        Objects.requireNonNull(id, "id is required");
        return responsibleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado: " + id));
    }

    public PageResult<Responsible> list(PageQuery pageQuery) {
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return responsibleRepository.findAll(pageQuery);
    }

    public Responsible update(UUID id, SaveResponsibleCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Responsible saved = transactions.execute(() -> {
            Responsible current = get(id);
            Responsible updated = new Responsible(
                    current.id(),
                    command.name(),
                    command.email(),
                    command.position(),
                    command.secretariatId(),
                    new AuditMetadata(current.audit().createdAt(), clock.instant()));

            validateSecretariat(updated.secretariatId());
            ensureEmailAvailable(updated.email(), updated.id());
            ensureLoginEmailAvailable(updated);
            return responsibleRepository.save(updated);
        });
        BusinessLog.info("responsavel.atualizado", "id=" + saved.id() + " ator=" + actor.auditLabel());
        return saved;
    }

    public void delete(UUID id, Actor actor) {
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Responsible responsible = transactions.execute(() -> {
            Responsible current = get(id);
            if (projectRepository.existsByResponsibleId(current.id())) {
                throw new ConflictException(
                        "O responsável está vinculado a ao menos um projeto e não pode ser excluído.");
            }
            responsibleRepository.deleteById(current.id());
            return current;
        });
        BusinessLog.info("responsavel.excluido", "id=" + responsible.id() + " ator=" + actor.auditLabel());
    }

    private void validateSecretariat(UUID secretariatId) {
        if (secretariatId != null && !secretariatRepository.existsById(secretariatId)) {
            throw new ResourceNotFoundException("Secretaria não encontrada: " + secretariatId);
        }
    }

    private void ensureLoginEmailAvailable(Responsible responsible) {
        if (credentialRepository.existsForResponsible(responsible.id())
                && credentialRepository.loginEmailTakenByAnotherUser(responsible.email(), responsible.id())) {
            throw new ConflictException(ResponsibleCredentialService.LOGIN_EMAIL_CONFLICT);
        }
    }

    private void ensureEmailAvailable(String email, UUID currentId) {
        boolean exists = currentId == null
                ? responsibleRepository.existsByEmail(email)
                : responsibleRepository.existsByEmailExcludingId(email, currentId);
        if (exists) {
            throw new ConflictException("Já existe responsável com este e-mail.");
        }
    }
}
