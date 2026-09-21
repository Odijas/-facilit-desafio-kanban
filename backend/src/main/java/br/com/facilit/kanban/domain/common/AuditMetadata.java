package br.com.facilit.kanban.domain.common;

import java.time.Instant;
import java.util.Objects;

public record AuditMetadata(Instant createdAt, Instant updatedAt) {

    public AuditMetadata {
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
    }
}
