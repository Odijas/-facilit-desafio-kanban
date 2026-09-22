package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryProjectRepository implements ProjectRepository {

    private final Map<UUID, Project> values = new LinkedHashMap<>();

    @Override
    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(values.get(id));
    }

    @Override
    public PageResult<Project> findAll(PageQuery pageQuery) {
        List<Project> sorted = values.values().stream()
                .sorted(Comparator.comparing(Project::name).thenComparing(Project::id))
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
    public PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery) {
        List<Project> sorted = values.values().stream()
                .filter(project -> project.status() == status)
                .sorted(Comparator.comparing(Project::name).thenComparing(Project::id))
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
    public Project save(Project project) {
        values.put(project.id(), project);
        return project;
    }

    @Override
    public void deleteById(UUID id) {
        values.remove(id);
    }

    @Override
    public boolean existsByResponsibleId(UUID responsibleId) {
        return values.values().stream()
                .anyMatch(project -> project.responsibleIds().contains(responsibleId));
    }
}
