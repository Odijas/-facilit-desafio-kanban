package br.com.facilit.kanban.infrastructure.persistence.secretariat;

import br.com.facilit.kanban.application.responsible.SecretariatRepository;
import java.util.UUID;
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
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
