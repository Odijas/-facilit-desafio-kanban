package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.domain.secretariat.Secretariat;

public record SecretariatResponse(
        String id,
        String name,
        String createdAt,
        String updatedAt) {

    static SecretariatResponse from(Secretariat secretariat) {
        return new SecretariatResponse(
                secretariat.id().toString(),
                secretariat.name(),
                secretariat.audit().createdAt().toString(),
                secretariat.audit().updatedAt().toString());
    }
}
