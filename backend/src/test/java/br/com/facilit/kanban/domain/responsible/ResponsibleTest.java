package br.com.facilit.kanban.domain.responsible;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResponsibleTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");

    @Test
    void normalizesEmailForStableUniqueness() {
        Responsible responsible = responsible("  ANA.SILVA@EXAMPLE.COM  ");

        assertThat(responsible.email()).isEqualTo("ana.silva@example.com");
    }

    @Test
    void rejectsMalformedEmail() {
        assertThatThrownBy(() -> responsible("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail inválido.");
    }

    private static Responsible responsible(String email) {
        return new Responsible(
                UUID.randomUUID(),
                "Ana Silva",
                email,
                "Analista",
                null,
                new AuditMetadata(NOW, NOW));
    }
}
