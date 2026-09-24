package br.com.facilit.kanban.domain.responsible;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.util.Locale;
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

        email = normalizeEmail(email);
    }

    private static String normalizeEmail(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.indexOf('@');
        boolean invalid = separator <= 0
                || separator != normalized.lastIndexOf('@')
                || separator == normalized.length() - 1
                || normalized.chars().anyMatch(Character::isWhitespace);
        if (invalid) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        return normalized;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatório: " + field);
        }
    }
}
