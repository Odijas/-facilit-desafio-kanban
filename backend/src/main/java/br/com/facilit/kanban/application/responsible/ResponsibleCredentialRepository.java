package br.com.facilit.kanban.application.responsible;

import java.time.Instant;
import java.util.UUID;

public interface ResponsibleCredentialRepository {

    void save(UUID responsibleId, String loginEmail, String rawPassword, Instant timestamp);

    boolean deleteByResponsibleId(UUID responsibleId);

    boolean existsForResponsible(UUID responsibleId);

    boolean loginEmailTakenByAnotherUser(String loginEmail, UUID responsibleId);
}
