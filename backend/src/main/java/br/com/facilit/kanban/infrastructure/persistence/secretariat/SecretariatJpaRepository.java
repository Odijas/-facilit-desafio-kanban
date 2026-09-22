package br.com.facilit.kanban.infrastructure.persistence.secretariat;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SecretariatJpaRepository extends JpaRepository<SecretariatJpaEntity, UUID> {
}
