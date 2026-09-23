package br.com.facilit.kanban.application.secretariat;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import java.util.Optional;
import java.util.UUID;

public interface SecretariatRepository {

    Optional<Secretariat> findById(UUID id);

    PageResult<Secretariat> findAll(PageQuery pageQuery);

    Secretariat save(Secretariat secretariat);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
