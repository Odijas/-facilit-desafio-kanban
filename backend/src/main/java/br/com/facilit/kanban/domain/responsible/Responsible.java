package br.com.facilit.kanban.domain.responsible;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.util.Objects;
import java.util.UUID;

public record Responsible(
        UUID id,
        String name,
        String email,
        String position,
        UUID secretariatId,
        AuditMetadata audit) {

    public Responsible {
        Objects.requireNonNull(id, "id is required");
        requireText(name, "name");
        requireText(email, "email");
        requireText(position, "position");
        Objects.requireNonNull(audit, "audit is required");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
