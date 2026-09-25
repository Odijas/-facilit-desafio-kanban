package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.delivery.common.ApiExamples;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import io.swagger.v3.oas.annotations.media.Schema;

public record SecretariatResponse(
        @Schema(example = ApiExamples.SECRETARIAT_ID) String id,
        @Schema(example = ApiExamples.SECRETARIAT_NAME) String name,
        @Schema(example = ApiExamples.CREATED_AT) String createdAt,
        @Schema(example = ApiExamples.UPDATED_AT) String updatedAt) {

    static SecretariatResponse from(Secretariat secretariat) {
        return new SecretariatResponse(
                secretariat.id().toString(),
                secretariat.name(),
                secretariat.audit().createdAt().toString(),
                secretariat.audit().updatedAt().toString());
    }
}
