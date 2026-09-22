package br.com.facilit.kanban.infrastructure.persistence.responsible;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsibleJpaRepository extends JpaRepository<ResponsibleJpaEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    long countByIdIn(Set<UUID> ids);
}
