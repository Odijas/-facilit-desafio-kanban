package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.delivery.common.ApiExamples;
import br.com.facilit.kanban.domain.responsible.Responsible;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record ResponsibleResponse(
        @Schema(example = ApiExamples.RESPONSIBLE_ID) UUID id,
        @Schema(example = ApiExamples.RESPONSIBLE_NAME) String name,
        @Schema(example = ApiExamples.RESPONSIBLE_EMAIL) String email,
        @Schema(example = ApiExamples.RESPONSIBLE_POSITION) String position,
        @Schema(example = ApiExamples.SECRETARIAT_ID) UUID secretariatId,
        @Schema(example = ApiExamples.CREATED_AT) Instant createdAt,
        @Schema(example = ApiExamples.UPDATED_AT) Instant updatedAt) {

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
