package br.com.facilit.kanban.domain.secretariat;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.util.Objects;
import java.util.UUID;

public record Secretariat(UUID id, String name, AuditMetadata audit) {

    public Secretariat {
        Objects.requireNonNull(id, "id is required");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatório: name");
        }

        Objects.requireNonNull(audit, "audit is required");
    }
}
