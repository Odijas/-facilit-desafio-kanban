package br.com.facilit.kanban.infrastructure.persistence.secretariat;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SecretariatPersistenceAdapter implements SecretariatRepository {

    private final SecretariatJpaRepository repository;

    public SecretariatPersistenceAdapter(SecretariatJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Secretariat> findById(UUID id) {
        return repository.findById(id).map(SecretariatPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Secretariat> findAll(PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(
                pageQuery.page(),
                pageQuery.size(),
                Sort.by("name").ascending().and(Sort.by("id").ascending()));
        var page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(SecretariatPersistenceAdapter::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    @Transactional
    public Secretariat save(Secretariat secretariat) {
        SecretariatJpaEntity entity = repository.findById(secretariat.id()).orElseGet(SecretariatJpaEntity::new);
        entity.replace(
                secretariat.id(),
                secretariat.name(),
                secretariat.audit().createdAt(),
                secretariat.audit().updatedAt());
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private static Secretariat toDomain(SecretariatJpaEntity entity) {
        return new Secretariat(
                entity.getId(),
                entity.getName(),
                new AuditMetadata(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
}
