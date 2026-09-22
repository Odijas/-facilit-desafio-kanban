package br.com.facilit.kanban.application.responsible;

import java.util.UUID;

public interface SecretariatRepository {

    boolean existsById(UUID id);
}
