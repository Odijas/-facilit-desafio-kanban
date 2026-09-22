package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.responsible.SecretariatRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class InMemorySecretariatRepository implements SecretariatRepository {

    private final Set<UUID> ids = new HashSet<>();

    public void add(UUID id) {
        ids.add(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return ids.contains(id);
    }
}
