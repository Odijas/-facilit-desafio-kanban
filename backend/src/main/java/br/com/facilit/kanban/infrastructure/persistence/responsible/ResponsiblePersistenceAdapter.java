package br.com.facilit.kanban.infrastructure.persistence.responsible;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.responsible.Responsible;
import br.com.facilit.kanban.infrastructure.security.SecurityUserJpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ResponsiblePersistenceAdapter implements ResponsibleRepository {

    private final ResponsibleJpaRepository repository;
    private final SecurityUserJpaRepository securityUserRepository;

    public ResponsiblePersistenceAdapter(
            ResponsibleJpaRepository repository,
            SecurityUserJpaRepository securityUserRepository) {
        this.repository = repository;
        this.securityUserRepository = securityUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Responsible> findById(UUID id) {
        return repository.findById(id).map(ResponsiblePersistenceAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Responsible> findAll(PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(
                pageQuery.page(),
                pageQuery.size(),
                Sort.by("name").ascending().and(Sort.by("id").ascending()));
        var page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(ResponsiblePersistenceAdapter::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    @Transactional
    public Responsible save(Responsible responsible) {
        ResponsibleJpaEntity entity = repository.findById(responsible.id())
                .orElseGet(ResponsibleJpaEntity::new);
        entity.replace(
                responsible.id(),
                responsible.name(),
                responsible.email(),
                responsible.position(),
                responsible.secretariatId(),
                responsible.audit().createdAt(),
                responsible.audit().updatedAt());
        Responsible saved = toDomain(repository.save(entity));
        securityUserRepository.findByResponsibleId(saved.id())
                .ifPresent(user -> user.changeEmail(saved.email(), saved.audit().updatedAt()));
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailExcludingId(String email, UUID id) {
        return repository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean allExist(Set<UUID> ids) {
        return !ids.isEmpty() && repository.countByIdIn(ids) == ids.size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySecretariatId(UUID secretariatId) {
        return repository.existsBySecretariatId(secretariatId);
    }

    private static Responsible toDomain(ResponsibleJpaEntity entity) {
        return new Responsible(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPosition(),
                entity.getSecretariatId(),
                new AuditMetadata(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
}
