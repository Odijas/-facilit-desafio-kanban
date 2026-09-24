package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusRequest(
        @NotNull @Schema(example = "IN_PROGRESS") ProjectStatus status,
        @Schema(
                example = "false",
                description = "Confirmação explícita. Obrigatória (true) nas transições que apagam uma data já "
                        + "registrada: Em andamento → A iniciar (início realizado) e Concluído → Em andamento ou "
                        + "Atrasado (término realizado). Sem ela, a API responde 422 CONFIRMATION_REQUIRED.")
        Boolean confirm) {

    public boolean confirmed() {
        return Boolean.TRUE.equals(confirm);
    }
}
