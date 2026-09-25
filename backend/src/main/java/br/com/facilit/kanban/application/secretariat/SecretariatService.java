package br.com.facilit.kanban.application.secretariat;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.BusinessLog;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.common.TransactionRunner;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SecretariatService {

    private final SecretariatRepository secretariatRepository;
    private final ResponsibleRepository responsibleRepository;
    private final TransactionRunner transactions;
    private final Clock clock;

    public SecretariatService(
            SecretariatRepository secretariatRepository,
            ResponsibleRepository responsibleRepository,
            TransactionRunner transactions,
            Clock clock) {
        this.secretariatRepository = Objects.requireNonNull(secretariatRepository);
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
    }

    public Secretariat create(SaveSecretariatCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Instant now = clock.instant();
        Secretariat secretariat = new Secretariat(
                UUID.randomUUID(),
                command.name(),
                new AuditMetadata(now, now));
        Secretariat saved = transactions.execute(() -> secretariatRepository.save(secretariat));
        BusinessLog.info("secretaria.criada", "id=" + saved.id() + " ator=" + actor.auditLabel());
        return saved;
    }

    public Secretariat get(UUID id) {
        Objects.requireNonNull(id, "id is required");
        return secretariatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria não encontrada: " + id));
    }

    public PageResult<Secretariat> list(PageQuery pageQuery) {
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return secretariatRepository.findAll(pageQuery);
    }

    public Secretariat update(UUID id, SaveSecretariatCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Secretariat saved = transactions.execute(() -> {
            Secretariat current = get(id);
            Secretariat updated = new Secretariat(
                    current.id(),
                    command.name(),
                    new AuditMetadata(current.audit().createdAt(), clock.instant()));
            return secretariatRepository.save(updated);
        });
        BusinessLog.info("secretaria.atualizada", "id=" + saved.id() + " ator=" + actor.auditLabel());
        return saved;
    }

    public void delete(UUID id, Actor actor) {
        Objects.requireNonNull(actor, "actor is required");
        actor.requireAdmin();
        Secretariat current = transactions.execute(() -> {
            Secretariat secretariat = get(id);
            if (responsibleRepository.existsBySecretariatId(secretariat.id())) {
                throw new ConflictException("A secretaria tem responsáveis vinculados e não pode ser excluída.");
            }
            secretariatRepository.deleteById(secretariat.id());
            return secretariat;
        });
        BusinessLog.info("secretaria.excluida", "id=" + current.id() + " ator=" + actor.auditLabel());
    }
}
