package br.com.facilit.kanban.application.responsible;

import java.util.UUID;

public record SaveResponsibleCommand(
        String name,
        String email,
        String position,
        UUID secretariatId) {
}
