package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.application.project.ProjectStatusIndicator;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryProjectRepository implements ProjectRepository {

    private final Map<UUID, Project> values = new LinkedHashMap<>();
    private final Map<UUID, UUID> responsibleSecretariats = new LinkedHashMap<>();

    public void assignResponsibleToSecretariat(UUID responsibleId, UUID secretariatId) {
        responsibleSecretariats.put(responsibleId, secretariatId);
    }

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
    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
        List<Project> sorted = values.values().stream()
                .filter(project -> filter.status() == null || project.status() == filter.status())
                .filter(project -> filter.responsibleId() == null
                        || project.responsibleIds().contains(filter.responsibleId()))
                .filter(project -> filter.secretariatId() == null
                        || project.responsibleIds().stream()
                                .map(responsibleSecretariats::get)
                                .anyMatch(filter.secretariatId()::equals))
                .filter(project -> filter.plannedFrom() == null
                        || project.dates().plannedEnd() != null
                                && !project.dates().plannedEnd().isBefore(filter.plannedFrom()))
                .filter(project -> filter.plannedTo() == null
                        || project.dates().plannedStart() != null
                                && !project.dates().plannedStart().isAfter(filter.plannedTo()))
                .filter(project -> filter.text() == null
                        || project.name().toLowerCase(Locale.ROOT).contains(filter.text().toLowerCase(Locale.ROOT)))
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
    public ProjectIndicators indicators() {
        Map<ProjectStatus, List<Project>> byStatus = new EnumMap<>(ProjectStatus.class);
        for (Project project : values.values()) {
            byStatus.computeIfAbsent(project.status(), ignored -> new java.util.ArrayList<>()).add(project);
        }
        List<ProjectStatusIndicator> summaries = Arrays.stream(ProjectStatus.values())
                .map(status -> {
                    List<Project> projects = byStatus.getOrDefault(status, List.of());
                    return new ProjectStatusIndicator(
                            status,
                            projects.size(),
                            projects.stream().mapToLong(Project::delayDays).average().orElse(0));
                })
                .toList();
        long delayed = values.values().stream().filter(project -> project.delayDays() > 0).count();
        return new ProjectIndicators(values.size(), delayed, summaries);
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
