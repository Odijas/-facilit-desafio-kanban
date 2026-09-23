package br.com.facilit.kanban.infrastructure.security;

import br.com.facilit.kanban.application.responsible.ResponsibleCredentialRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ResponsibleCredentialPersistenceAdapter implements ResponsibleCredentialRepository {

    private static final String RESPONSIBLE_ROLE = "RESPONSIBLE";

    private final SecurityUserJpaRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ResponsibleCredentialPersistenceAdapter(
            SecurityUserJpaRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void save(UUID responsibleId, String loginEmail, String rawPassword, Instant timestamp) {
        String passwordHash = passwordEncoder.encode(rawPassword);
        Optional<SecurityUserJpaEntity> existing = repository.findByResponsibleId(responsibleId);
        if (existing.isPresent()) {
            existing.get().replaceCredentials(loginEmail, passwordHash, timestamp);
            return;
        }
        repository.save(new SecurityUserJpaEntity(
                UUID.randomUUID(),
                loginEmail,
                passwordHash,
                RESPONSIBLE_ROLE,
                true,
                responsibleId,
                timestamp,
                timestamp));
    }

    @Override
    @Transactional
    public boolean deleteByResponsibleId(UUID responsibleId) {
        Optional<SecurityUserJpaEntity> existing = repository.findByResponsibleId(responsibleId);
        existing.ifPresent(repository::delete);
        return existing.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForResponsible(UUID responsibleId) {
        return repository.existsByResponsibleId(responsibleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean loginEmailTakenByAnotherUser(String loginEmail, UUID responsibleId) {
        return repository.existsByEmailIgnoreCaseAndResponsibleIdIsNull(loginEmail)
                || repository.existsByEmailIgnoreCaseAndResponsibleIdNot(loginEmail, responsibleId);
    }
}
