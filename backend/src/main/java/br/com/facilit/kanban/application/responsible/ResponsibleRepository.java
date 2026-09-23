package br.com.facilit.kanban.application.responsible;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResponsibleRepository {

    Optional<Responsible> findById(UUID id);

    PageResult<Responsible> findAll(PageQuery pageQuery);

    Responsible save(Responsible responsible);

    void deleteById(UUID id);

    boolean existsByEmail(String email);

    boolean existsByEmailExcludingId(String email, UUID id);

    boolean allExist(Set<UUID> ids);

    boolean existsBySecretariatId(UUID secretariatId);
}
