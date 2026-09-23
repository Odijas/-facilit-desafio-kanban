package br.com.facilit.kanban.application.responsible;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
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
    private final Clock clock;

    public ResponsibleService(
            ResponsibleRepository responsibleRepository,
            SecretariatRepository secretariatRepository,
            ProjectRepository projectRepository,
            Clock clock) {
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.secretariatRepository = Objects.requireNonNull(secretariatRepository);
        this.projectRepository = Objects.requireNonNull(projectRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public Responsible create(SaveResponsibleCommand command) {
        Objects.requireNonNull(command, "command is required");
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
    }

    public Responsible get(UUID id) {
        Objects.requireNonNull(id, "id is required");
        return responsibleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsible not found: " + id));
    }

    public PageResult<Responsible> list(PageQuery pageQuery) {
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return responsibleRepository.findAll(pageQuery);
    }

    public Responsible update(UUID id, SaveResponsibleCommand command) {
        Objects.requireNonNull(command, "command is required");
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
        return responsibleRepository.save(updated);
    }

    public void delete(UUID id) {
        Responsible responsible = get(id);
        if (projectRepository.existsByResponsibleId(responsible.id())) {
            throw new ConflictException("Responsible is assigned to at least one project");
        }
        responsibleRepository.deleteById(responsible.id());
    }

    private void validateSecretariat(UUID secretariatId) {
        if (secretariatId != null && !secretariatRepository.existsById(secretariatId)) {
            throw new ResourceNotFoundException("Secretariat not found: " + secretariatId);
        }
    }

    private void ensureEmailAvailable(String email, UUID currentId) {
        boolean exists = currentId == null
                ? responsibleRepository.existsByEmail(email)
                : responsibleRepository.existsByEmailExcludingId(email, currentId);
        if (exists) {
            throw new ConflictException("Responsible email already exists");
        }
    }
}
