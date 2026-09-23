package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class InMemoryResponsibleRepository implements ResponsibleRepository {

    private final Map<UUID, Responsible> values = new LinkedHashMap<>();

    @Override
    public Optional<Responsible> findById(UUID id) {
        return Optional.ofNullable(values.get(id));
    }

    @Override
    public PageResult<Responsible> findAll(PageQuery pageQuery) {
        List<Responsible> sorted = values.values().stream()
                .sorted(Comparator.comparing(Responsible::name).thenComparing(Responsible::id))
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
    public Responsible save(Responsible responsible) {
        values.put(responsible.id(), responsible);
        return responsible;
    }

    @Override
    public void deleteById(UUID id) {
        values.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return values.values().stream().anyMatch(value -> value.email().equals(email));
    }

    @Override
    public boolean existsByEmailExcludingId(String email, UUID id) {
        return values.values().stream()
                .anyMatch(value -> !value.id().equals(id) && value.email().equals(email));
    }

    @Override
    public boolean allExist(Set<UUID> ids) {
        return !ids.isEmpty() && ids.stream().allMatch(values::containsKey);
    }

    @Override
    public boolean existsBySecretariatId(UUID secretariatId) {
        return values.values().stream()
                .anyMatch(value -> secretariatId.equals(value.secretariatId()));
    }
}
