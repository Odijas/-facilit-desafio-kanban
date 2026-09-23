package br.com.facilit.kanban.application.secretariat;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
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
    private final Clock clock;

    public SecretariatService(
            SecretariatRepository secretariatRepository,
            ResponsibleRepository responsibleRepository,
            Clock clock) {
        this.secretariatRepository = Objects.requireNonNull(secretariatRepository);
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public Secretariat create(SaveSecretariatCommand command) {
        Objects.requireNonNull(command, "command is required");
        Instant now = clock.instant();
        Secretariat secretariat = new Secretariat(
                UUID.randomUUID(),
                command.name(),
                new AuditMetadata(now, now));
        return secretariatRepository.save(secretariat);
    }

    public Secretariat get(UUID id) {
        Objects.requireNonNull(id, "id is required");
        return secretariatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretariat not found: " + id));
    }

    public PageResult<Secretariat> list(PageQuery pageQuery) {
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return secretariatRepository.findAll(pageQuery);
    }

    public Secretariat update(UUID id, SaveSecretariatCommand command) {
        Objects.requireNonNull(command, "command is required");
        Secretariat current = get(id);
        Secretariat updated = new Secretariat(
                current.id(),
                command.name(),
                new AuditMetadata(current.audit().createdAt(), clock.instant()));
        return secretariatRepository.save(updated);
    }

    public void delete(UUID id) {
        Secretariat current = get(id);
        if (responsibleRepository.existsBySecretariatId(current.id())) {
            throw new ConflictException("Secretariat is assigned to at least one responsible");
        }
        secretariatRepository.deleteById(current.id());
    }
}
