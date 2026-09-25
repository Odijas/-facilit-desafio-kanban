package br.com.facilit.kanban.delivery.rest;

import static br.com.facilit.kanban.delivery.common.InputLimits.NAME_MAX_LENGTH;

import br.com.facilit.kanban.delivery.common.ApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecretariatRequest(
        @NotBlank @Size(max = NAME_MAX_LENGTH) @Schema(example = ApiExamples.SECRETARIAT_NAME) String name) {
}
