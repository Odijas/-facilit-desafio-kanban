package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.domain.responsible.Responsible;
import java.time.Instant;
import java.util.UUID;

public record ResponsibleResponse(
        UUID id,
        String name,
        String email,
        String position,
        UUID secretariatId,
        Instant createdAt,
        Instant updatedAt) {

    public static ResponsibleResponse from(Responsible responsible) {
        return new ResponsibleResponse(
                responsible.id(),
                responsible.name(),
                responsible.email(),
                responsible.position(),
                responsible.secretariatId(),
                responsible.audit().createdAt(),
                responsible.audit().updatedAt());
    }
}
