package br.com.facilit.kanban.infrastructure.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityUserJpaRepository extends JpaRepository<SecurityUserJpaEntity, UUID> {

    Optional<SecurityUserJpaEntity> findByEmailIgnoreCase(String email);
}
