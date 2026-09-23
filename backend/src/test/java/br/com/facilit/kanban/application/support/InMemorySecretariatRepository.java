package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemorySecretariatRepository implements SecretariatRepository {

    private final Map<UUID, Secretariat> values = new LinkedHashMap<>();

    @Override
    public Optional<Secretariat> findById(UUID id) {
        return Optional.ofNullable(values.get(id));
    }

    @Override
    public PageResult<Secretariat> findAll(PageQuery pageQuery) {
        List<Secretariat> sorted = values.values().stream()
                .sorted(Comparator.comparing(Secretariat::name).thenComparing(Secretariat::id))
                .toList();
        int fromIndex = Math.min(pageQuery.page() * pageQuery.size(), sorted.size());
        int toIndex = Math.min(fromIndex + pageQuery.size(), sorted.size());
        int totalPages = sorted.isEmpty()
                ? 0
                : (int) Math.ceil((double) sorted.size() / pageQuery.size());
        return new PageResult<>(
                sorted.subList(fromIndex, toIndex),
                pageQuery.page(),
                pageQuery.size(),
                sorted.size(),
                totalPages);
    }

    @Override
    public Secretariat save(Secretariat secretariat) {
        values.put(secretariat.id(), secretariat);
        return secretariat;
    }

    @Override
    public void deleteById(UUID id) {
        values.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return values.containsKey(id);
    }
}
